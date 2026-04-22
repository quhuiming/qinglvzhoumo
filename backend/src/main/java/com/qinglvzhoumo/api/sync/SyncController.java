package com.qinglvzhoumo.api.sync;

import com.qinglvzhoumo.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
  private final SyncService syncService;

  public SyncController(SyncService syncService) {
    this.syncService = syncService;
  }

  @GetMapping
  public SyncDtos.SyncResponse pull(AuthenticatedUser user, @RequestParam(required = false) String since) {
    return syncService.pull(user, since);
  }

  @PostMapping
  public SyncDtos.SyncResponse push(AuthenticatedUser user, @Valid @RequestBody SyncDtos.SyncPushRequest request) {
    return syncService.push(user, request);
  }
}
