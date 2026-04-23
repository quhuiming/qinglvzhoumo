package com.qinglvzhoumo.api.user;

import com.qinglvzhoumo.api.common.ApiException;
import com.qinglvzhoumo.api.couple.CoupleMember;
import com.qinglvzhoumo.api.couple.CoupleMemberRepository;
import com.qinglvzhoumo.api.security.AuthenticatedUser;
import com.qinglvzhoumo.api.sync.SyncItemRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final String ANONYMOUS = "ANONYMOUS";
  private static final String REGISTERED = "REGISTERED";
  private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
  private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,32}$");

  private final UserAccountRepository userRepository;
  private final UserSessionRepository sessionRepository;
  private final CoupleMemberRepository memberRepository;
  private final SyncItemRepository syncItemRepository;
  private final SecureRandom secureRandom = new SecureRandom();
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(
      UserAccountRepository userRepository,
      UserSessionRepository sessionRepository,
      CoupleMemberRepository memberRepository,
      SyncItemRepository syncItemRepository
  ) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.memberRepository = memberRepository;
    this.syncItemRepository = syncItemRepository;
  }

  @Transactional
  public AuthDtos.AuthResponse anonymousLogin(AuthDtos.AnonymousLoginRequest request) {
    UserAccount user = userRepository.findByDeviceIdAndAccountType(request.deviceId(), ANONYMOUS)
        .orElseGet(() -> createAnonymousUser(request.deviceId()));
    if (request.nickname() != null && !request.nickname().isBlank()) {
      user.setNickname(request.nickname().trim());
    }
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    return response(user, createSession(user, request.deviceId()).getToken());
  }

  @Transactional
  public AuthDtos.AuthResponse register(AuthenticatedUser currentUser, AuthDtos.RegisterRequest request) {
    UserAccount user = userRepository.findById(currentUser.id())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first"));
    if (REGISTERED.equals(user.getAccountType())) {
      throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REGISTERED", "This device is already registered");
    }
    String phone = normalizePhone(request.phone());
    validatePassword(request.password());
    if (userRepository.existsByPhone(phone)) {
      throw new ApiException(HttpStatus.CONFLICT, "PHONE_ALREADY_REGISTERED", "Phone is already registered");
    }
    if (request.nickname() != null && !request.nickname().isBlank()) {
      user.setNickname(request.nickname().trim());
    }
    user.setPhone(phone);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setAccountType(REGISTERED);
    user.setDeviceId(null);
    user.setRegisteredAt(Instant.now());
    user.setLastLoginAt(Instant.now());
    user = userRepository.save(user);
    return response(user, createSession(user, null).getToken());
  }

  @Transactional
  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
    String phone = normalizePhone(request.phone());
    UserAccount target = userRepository.findByPhone(phone)
        .filter(user -> REGISTERED.equals(user.getAccountType()))
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Phone or password is incorrect"));
    if (target.getPasswordHash() == null || !passwordEncoder.matches(request.password(), target.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Phone or password is incorrect");
    }

    UserAccount deviceAnonymous = userRepository.findByDeviceIdAndAccountType(request.deviceId(), ANONYMOUS).orElse(null);
    if (deviceAnonymous != null && !deviceAnonymous.getId().equals(target.getId())) {
      maybeTransferAnonymousSpace(deviceAnonymous, target);
      deviceAnonymous.setDeviceId(null);
      userRepository.save(deviceAnonymous);
    }

    target.setLastLoginAt(Instant.now());
    target = userRepository.save(target);
    return response(target, createSession(target, request.deviceId()).getToken());
  }

  @Transactional
  public void logout(String token) {
    sessionRepository.findByTokenAndRevokedAtIsNull(token).ifPresent(session -> {
      session.setRevokedAt(Instant.now());
      sessionRepository.save(session);
    });
  }

  @Transactional(readOnly = true)
  public AuthDtos.AuthResponse me(AuthenticatedUser user, String token) {
    return response(user.account(), token);
  }

  private void maybeTransferAnonymousSpace(UserAccount anonymousUser, UserAccount targetUser) {
    CoupleMember anonymousMember = memberRepository.findByUserId(anonymousUser.getId()).orElse(null);
    if (anonymousMember == null) return;
    CoupleMember targetMember = memberRepository.findByUserId(targetUser.getId()).orElse(null);
    if (targetMember != null && !targetMember.getCoupleId().equals(anonymousMember.getCoupleId())) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "COUPLE_SPACE_CONFLICT",
          "Current device is bound to another couple space. Please leave it first or use the matching account."
      );
    }
    if (targetMember == null) {
      memberRepository.transferUser(anonymousUser.getId(), targetUser.getId());
      syncItemRepository.transferAuthor(anonymousUser.getId(), targetUser.getId());
    }
  }

  private UserAccount createAnonymousUser(String deviceId) {
    UserAccount user = new UserAccount();
    user.setDeviceId(deviceId);
    user.setNickname("qinglv");
    user.setAccountType(ANONYMOUS);
    user.setAccessToken(generateToken());
    return userRepository.save(user);
  }

  private UserSession createSession(UserAccount user, String deviceId) {
    UserSession session = new UserSession();
    session.setToken(generateToken());
    session.setUserId(user.getId());
    session.setDeviceId(deviceId);
    return sessionRepository.save(session);
  }

  private AuthDtos.AuthResponse response(UserAccount user, String token) {
    Long coupleId = memberRepository.findByUserId(user.getId())
        .map(CoupleMember::getCoupleId)
        .orElse(null);
    boolean registered = REGISTERED.equals(user.getAccountType());
    return new AuthDtos.AuthResponse(
        user.getId(),
        token,
        user.getNickname(),
        coupleId,
        user.getPhone(),
        registered
    );
  }

  private String normalizePhone(String phone) {
    String value = phone == null ? "" : phone.trim();
    if (!PHONE_PATTERN.matcher(value).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "Phone number format is invalid");
    }
    return value;
  }

  private void validatePassword(String password) {
    if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "Password must be 8-32 chars and include letters and numbers");
    }
  }

  private String generateToken() {
    byte[] bytes = new byte[36];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
