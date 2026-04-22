package com.qinglvzhoumo.api.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByDeviceId(String deviceId);

  Optional<UserAccount> findByAccessToken(String accessToken);
}
