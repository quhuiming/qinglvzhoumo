package com.qinglvzhoumo.api.couple;

import com.qinglvzhoumo.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/couples")
public class CoupleController {
  private final CoupleService coupleService;

  public CoupleController(CoupleService coupleService) {
    this.coupleService = coupleService;
  }

  @PostMapping("/invite")
  public CoupleDtos.InviteResponse createInvite(AuthenticatedUser user) {
    return coupleService.createOrGetInvite(user);
  }

  @PostMapping("/join")
  public CoupleDtos.CoupleResponse join(AuthenticatedUser user, @Valid @RequestBody CoupleDtos.JoinRequest request) {
    return coupleService.join(user, request);
  }
}
