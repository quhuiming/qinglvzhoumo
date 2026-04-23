package com.qinglvzhoumo.api.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByDeviceId(String deviceId);

  Optional<UserAccount> findByDeviceIdAndAccountType(String deviceId, String accountType);

  Optional<UserAccount> findByAccessToken(String accessToken);

  Optional<UserAccount> findByPhone(String phone);

  boolean existsByPhone(String phone);
}
