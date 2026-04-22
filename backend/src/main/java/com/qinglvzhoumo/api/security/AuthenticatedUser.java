package com.qinglvzhoumo.api.security;

import com.qinglvzhoumo.api.user.UserAccount;

public record AuthenticatedUser(UserAccount account) {
  public Long id() {
    return account.getId();
  }
}
