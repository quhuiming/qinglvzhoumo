package com.qinglvzhoumo.api.sync;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SyncDtos {
  public record SyncItemRequest(
      @NotBlank(message = "type 不能为空") String type,
      @NotBlank(message = "clientId 不能为空") String clientId,
      JsonNode payload,
      Long version,
      Long authorUserId,
      String updatedAt,
      String deletedAt
  ) {}

  public record SyncPushRequest(@NotEmpty(message = "items 不能为空") List<@Valid SyncItemRequest> items) {}

  public record SyncItemResponse(
      String type,
      String clientId,
      JsonNode payload,
      Long version,
      Long authorUserId,
      String updatedAt,
      String deletedAt,
      String serverUpdatedAt
  ) {}

  public record SyncResponse(List<SyncItemResponse> items, String serverTime) {}
}
