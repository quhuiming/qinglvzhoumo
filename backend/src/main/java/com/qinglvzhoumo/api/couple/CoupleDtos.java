package com.qinglvzhoumo.api.couple;

import jakarta.validation.constraints.NotBlank;

public class CoupleDtos {
  public record InviteResponse(Long coupleId, String inviteCode, long memberCount) {}

  public record JoinRequest(@NotBlank(message = "邀请码不能为空") String inviteCode) {}

  public record CoupleResponse(Long coupleId, String inviteCode, long memberCount) {}
}
