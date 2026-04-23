package com.qinglvzhoumo.api.couple;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {
  Optional<CoupleMember> findByUserId(Long userId);

  long countByCoupleId(Long coupleId);

  List<CoupleMember> findAllByCoupleId(Long coupleId);

  void deleteByUserId(Long userId);

  @Modifying
  @Query("update CoupleMember member set member.userId = :toUserId where member.userId = :fromUserId")
  int transferUser(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}
