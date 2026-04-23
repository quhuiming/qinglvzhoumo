package com.qinglvzhoumo.api.sync;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncItemRepository extends JpaRepository<SyncItem, Long> {
  Optional<SyncItem> findByCoupleIdAndTypeAndClientId(Long coupleId, String type, String clientId);

  List<SyncItem> findAllByCoupleIdOrderByServerUpdatedAtDesc(Long coupleId);

  List<SyncItem> findAllByCoupleIdAndServerUpdatedAtAfterOrderByServerUpdatedAtDesc(Long coupleId, Instant since);

  @Modifying
  @Query("update SyncItem item set item.authorUserId = :toUserId where item.authorUserId = :fromUserId")
  int transferAuthor(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}
