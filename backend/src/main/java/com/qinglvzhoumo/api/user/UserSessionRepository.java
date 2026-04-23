package com.qinglvzhoumo.api.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  Optional<UserSession> findByTokenAndRevokedAtIsNull(String token);
}
