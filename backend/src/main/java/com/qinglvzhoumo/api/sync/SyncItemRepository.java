package com.qinglvzhoumo.api.sync;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncItemRepository extends JpaRepository<SyncItem, Long> {
  Optional<SyncItem> findByCoupleIdAndTypeAndClientId(Long coupleId, String type, String clientId);

  List<SyncItem> findAllByCoupleIdOrderByServerUpdatedAtDesc(Long coupleId);

  List<SyncItem> findAllByCoupleIdAndServerUpdatedAtAfterOrderByServerUpdatedAtDesc(Long coupleId, Instant since);
}
