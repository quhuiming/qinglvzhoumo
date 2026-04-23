package com.qinglvzhoumo.api.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "sync_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_sync_item_client",
        columnNames = {"couple_id", "type", "client_id"}
    )
)
public class SyncItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "couple_id", nullable = false)
  private Long coupleId;

  @Column(name = "author_user_id")
  private Long authorUserId;

  @Column(nullable = false, length = 32)
  private String type;

  @Column(name = "client_id", nullable = false, length = 96)
  private String clientId;

  @Lob
  @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
  private String payload;

  @Column(nullable = false)
  private Long version;

  @Column(length = 32)
  private String clientUpdatedAt;

  @Column(length = 32)
  private String deletedAt;

  @Column(nullable = false)
  private Instant serverUpdatedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    serverUpdatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    serverUpdatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Long getCoupleId() {
    return coupleId;
  }

  public void setCoupleId(Long coupleId) {
    this.coupleId = coupleId;
  }

  public Long getAuthorUserId() {
    return authorUserId;
  }

  public void setAuthorUserId(Long authorUserId) {
    this.authorUserId = authorUserId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getClientUpdatedAt() {
    return clientUpdatedAt;
  }

  public void setClientUpdatedAt(String clientUpdatedAt) {
    this.clientUpdatedAt = clientUpdatedAt;
  }

  public String getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(String deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Instant getServerUpdatedAt() {
    return serverUpdatedAt;
  }
}
