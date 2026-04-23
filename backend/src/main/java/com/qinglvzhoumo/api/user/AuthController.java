package com.qinglvzhoumo.api.user;

import com.qinglvzhoumo.api.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/anonymous")
  public AuthDtos.AuthResponse anonymousLogin(@Valid @RequestBody AuthDtos.AnonymousLoginRequest request) {
    return authService.anonymousLogin(request);
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(
      AuthenticatedUser user,
      @Valid @RequestBody AuthDtos.RegisterRequest request
  ) {
    return authService.register(user, request);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request) {
    authService.logout(tokenFrom(request));
  }

  @GetMapping("/me")
  public AuthDtos.AuthResponse me(AuthenticatedUser user, HttpServletRequest request) {
    return authService.me(user, tokenFrom(request));
  }

  private String tokenFrom(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) return "";
    return header.substring("Bearer ".length()).trim();
  }
}
