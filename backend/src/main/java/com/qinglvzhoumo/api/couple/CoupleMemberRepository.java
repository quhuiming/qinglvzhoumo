package com.qinglvzhoumo.api.couple;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {
  Optional<CoupleMember> findByUserId(Long userId);

  long countByCoupleId(Long coupleId);

  List<CoupleMember> findAllByCoupleId(Long coupleId);

  void deleteByUserId(Long userId);
}
