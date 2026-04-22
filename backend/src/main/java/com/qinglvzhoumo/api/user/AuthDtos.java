package com.qinglvzhoumo.api.user;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
  public record AnonymousLoginRequest(
      @NotBlank(message = "deviceId 不能为空") String deviceId,
      String nickname
  ) {}

  public record AuthResponse(
      Long userId,
      String token,
      String nickname,
      Long coupleId
  ) {}
}
