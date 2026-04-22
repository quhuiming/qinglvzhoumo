package com.qinglvzhoumo.api.user;

import com.qinglvzhoumo.api.couple.CoupleMemberRepository;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserAccountRepository userRepository;
  private final CoupleMemberRepository memberRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public AuthService(UserAccountRepository userRepository, CoupleMemberRepository memberRepository) {
    this.userRepository = userRepository;
    this.memberRepository = memberRepository;
  }

  @Transactional
  public AuthDtos.AuthResponse anonymousLogin(AuthDtos.AnonymousLoginRequest request) {
    UserAccount user = userRepository.findByDeviceId(request.deviceId())
        .orElseGet(() -> createUser(request.deviceId()));
    if (request.nickname() != null && !request.nickname().isBlank()) {
      user.setNickname(request.nickname().trim());
    }
    userRepository.save(user);
    Long coupleId = memberRepository.findByUserId(user.getId())
        .map(member -> member.getCoupleId())
        .orElse(null);
    return new AuthDtos.AuthResponse(user.getId(), user.getAccessToken(), user.getNickname(), coupleId);
  }

  private UserAccount createUser(String deviceId) {
    UserAccount user = new UserAccount();
    user.setDeviceId(deviceId);
    user.setNickname("我");
    user.setAccessToken(generateToken());
    return userRepository.save(user);
  }

  private String generateToken() {
    byte[] bytes = new byte[36];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
