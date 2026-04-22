package com.qinglvzhoumo.api.couple;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
  Optional<Couple> findByInviteCode(String inviteCode);

  boolean existsByInviteCode(String inviteCode);
}
