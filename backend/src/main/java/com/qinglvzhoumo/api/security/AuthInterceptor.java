package com.qinglvzhoumo.api.security;

import com.qinglvzhoumo.api.common.ApiException;
import com.qinglvzhoumo.api.user.UserAccount;
import com.qinglvzhoumo.api.user.UserAccountRepository;
import com.qinglvzhoumo.api.user.UserSession;
import com.qinglvzhoumo.api.user.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  public static final String REQUEST_USER = "authenticatedUser";
  private final UserAccountRepository userRepository;
  private final UserSessionRepository sessionRepository;

  public AuthInterceptor(UserAccountRepository userRepository, UserSessionRepository sessionRepository) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
  }

  @Override
  @Transactional
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first");
    }
    String token = header.substring("Bearer ".length()).trim();
    request.setAttribute(REQUEST_USER, new AuthenticatedUser(findByToken(token)));
    return true;
  }

  private UserAccount findByToken(String token) {
    UserSession session = sessionRepository.findByTokenAndRevokedAtIsNull(token).orElse(null);
    if (session != null) {
      session.setLastSeenAt(Instant.now());
      sessionRepository.save(session);
      return userRepository.findById(session.getUserId())
          .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Login expired"));
    }
    return userRepository.findByAccessToken(token)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Login expired"));
  }
}
