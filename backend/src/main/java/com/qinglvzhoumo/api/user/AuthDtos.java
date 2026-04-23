package com.qinglvzhoumo.api.user;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
  public record AnonymousLoginRequest(
      @NotBlank(message = "deviceId cannot be blank") String deviceId,
      String nickname
  ) {}

  public record RegisterRequest(
      @NotBlank(message = "phone cannot be blank") String phone,
      @NotBlank(message = "password cannot be blank") String password,
      String nickname
  ) {}

  public record LoginRequest(
      @NotBlank(message = "phone cannot be blank") String phone,
      @NotBlank(message = "password cannot be blank") String password,
      @NotBlank(message = "deviceId cannot be blank") String deviceId
  ) {}

  public record AuthResponse(
      Long userId,
      String token,
      String nickname,
      Long coupleId,
      String phone,
      boolean registered
  ) {}
}
