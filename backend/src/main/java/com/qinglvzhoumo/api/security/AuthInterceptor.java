package com.qinglvzhoumo.api.security;

import com.qinglvzhoumo.api.common.ApiException;
import com.qinglvzhoumo.api.user.UserAccount;
import com.qinglvzhoumo.api.user.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  public static final String REQUEST_USER = "authenticatedUser";
  private final UserAccountRepository userRepository;

  public AuthInterceptor(UserAccountRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "请先登录");
    }
    String token = header.substring("Bearer ".length()).trim();
    UserAccount user = userRepository.findByAccessToken(token)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "登录已失效"));
    request.setAttribute(REQUEST_USER, new AuthenticatedUser(user));
    return true;
  }
}
