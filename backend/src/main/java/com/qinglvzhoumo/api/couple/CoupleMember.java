package com.qinglvzhoumo.api.couple;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "couple_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_couple_member_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_couple_member_pair", columnNames = {"couple_id", "user_id"})
    }
)
public class CoupleMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "couple_id", nullable = false)
  private Long coupleId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 16)
  private String role;

  @Column(nullable = false)
  private Instant joinedAt;

  @PrePersist
  void prePersist() {
    joinedAt = Instant.now();
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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
