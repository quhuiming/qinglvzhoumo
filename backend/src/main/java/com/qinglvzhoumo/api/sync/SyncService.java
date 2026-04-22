package com.qinglvzhoumo.api.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qinglvzhoumo.api.common.ApiException;
import com.qinglvzhoumo.api.couple.CoupleService;
import com.qinglvzhoumo.api.security.AuthenticatedUser;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncService {
  private final SyncItemRepository syncItemRepository;
  private final CoupleService coupleService;
  private final ObjectMapper objectMapper;

  public SyncService(SyncItemRepository syncItemRepository, CoupleService coupleService, ObjectMapper objectMapper) {
    this.syncItemRepository = syncItemRepository;
    this.coupleService = coupleService;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public SyncDtos.SyncResponse pull(AuthenticatedUser user, String since) {
    Long coupleId = coupleService.requireCoupleId(user);
    List<SyncItem> items = since == null || since.isBlank()
        ? syncItemRepository.findAllByCoupleIdOrderByServerUpdatedAtDesc(coupleId)
        : syncItemRepository.findAllByCoupleIdAndServerUpdatedAtAfterOrderByServerUpdatedAtDesc(coupleId, parseInstant(since));
    return new SyncDtos.SyncResponse(items.stream().map(this::toResponse).toList(), Instant.now().toString());
  }

  @Transactional
  public SyncDtos.SyncResponse push(AuthenticatedUser user, SyncDtos.SyncPushRequest request) {
    Long coupleId = coupleService.requireCoupleId(user);
    List<SyncItem> saved = request.items().stream()
        .map(item -> upsert(coupleId, item))
        .toList();
    return new SyncDtos.SyncResponse(saved.stream().map(this::toResponse).toList(), Instant.now().toString());
  }

  private SyncItem upsert(Long coupleId, SyncDtos.SyncItemRequest request) {
    SyncItem item = syncItemRepository
        .findByCoupleIdAndTypeAndClientId(coupleId, request.type(), request.clientId())
        .orElseGet(SyncItem::new);
    item.setCoupleId(coupleId);
    item.setType(request.type());
    item.setClientId(request.clientId());
    item.setPayload(writePayload(request.payload()));
    item.setClientUpdatedAt(request.updatedAt());
    item.setDeletedAt(request.deletedAt() == null ? "" : request.deletedAt());
    item.setVersion(item.getVersion() == null ? 1 : item.getVersion() + 1);
    return syncItemRepository.save(item);
  }

  private SyncDtos.SyncItemResponse toResponse(SyncItem item) {
    return new SyncDtos.SyncItemResponse(
        item.getType(),
        item.getClientId(),
        readPayload(item.getPayload()),
        item.getVersion(),
        item.getClientUpdatedAt(),
        item.getDeletedAt(),
        item.getServerUpdatedAt().toString()
    );
  }

  private String writePayload(JsonNode payload) {
    try {
      return objectMapper.writeValueAsString(payload == null ? objectMapper.createObjectNode() : payload);
    } catch (JsonProcessingException error) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD", "同步内容格式不正确");
    }
  }

  private JsonNode readPayload(String payload) {
    try {
      return objectMapper.readTree(payload);
    } catch (JsonProcessingException error) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "BROKEN_PAYLOAD", "同步内容读取失败");
    }
  }

  private Instant parseInstant(String value) {
    try {
      return Instant.parse(value);
    } catch (RuntimeException error) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SINCE", "since 必须是 ISO 时间");
    }
  }
}
