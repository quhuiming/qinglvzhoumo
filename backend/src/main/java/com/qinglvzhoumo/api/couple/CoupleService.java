package com.qinglvzhoumo.api.couple;

import com.qinglvzhoumo.api.common.ApiException;
import com.qinglvzhoumo.api.security.AuthenticatedUser;
import java.security.SecureRandom;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoupleService {
  private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private final CoupleRepository coupleRepository;
  private final CoupleMemberRepository memberRepository;
  private final SecureRandom random = new SecureRandom();

  public CoupleService(CoupleRepository coupleRepository, CoupleMemberRepository memberRepository) {
    this.coupleRepository = coupleRepository;
    this.memberRepository = memberRepository;
  }

  @Transactional
  public CoupleDtos.InviteResponse createOrGetInvite(AuthenticatedUser user) {
    CoupleMember existingMember = memberRepository.findByUserId(user.id()).orElse(null);
    Couple couple;
    if (existingMember == null) {
      couple = new Couple();
      couple.setInviteCode(generateUniqueInviteCode());
      couple.setCreatedByUserId(user.id());
      couple = coupleRepository.save(couple);
      addMember(couple.getId(), user.id(), "OWNER");
    } else {
      couple = coupleRepository.findById(existingMember.getCoupleId())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COUPLE_NOT_FOUND", "情侣空间不存在"));
    }
    return new CoupleDtos.InviteResponse(
        couple.getId(),
        couple.getInviteCode(),
        memberRepository.countByCoupleId(couple.getId())
    );
  }

  @Transactional
  public CoupleDtos.CoupleResponse join(AuthenticatedUser user, CoupleDtos.JoinRequest request) {
    Couple couple = coupleRepository.findByInviteCode(request.inviteCode().trim().toUpperCase())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "邀请码不存在"));
    CoupleMember existingMember = memberRepository.findByUserId(user.id()).orElse(null);
    if (existingMember != null && existingMember.getCoupleId().equals(couple.getId())) {
      return response(couple);
    }
    if (existingMember != null) {
      throw new ApiException(HttpStatus.CONFLICT, "ALREADY_BOUND", "你已经绑定了情侣空间");
    }
    if (memberRepository.countByCoupleId(couple.getId()) >= 2) {
      throw new ApiException(HttpStatus.CONFLICT, "COUPLE_FULL", "这个邀请码已经被使用");
    }
    addMember(couple.getId(), user.id(), "PARTNER");
    return response(couple);
  }

  @Transactional(readOnly = true)
  public CoupleDtos.CoupleStatusResponse status(AuthenticatedUser user) {
    CoupleMember member = memberRepository.findByUserId(user.id()).orElse(null);
    if (member == null || member.getCoupleId() == null) {
      return new CoupleDtos.CoupleStatusResponse(
          user.id(),
          user.account().getNickname(),
          null,
          "",
          0
      );
    }
    Couple couple = coupleRepository.findById(member.getCoupleId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COUPLE_NOT_FOUND", "情侣空间不存在"));
    return new CoupleDtos.CoupleStatusResponse(
        user.id(),
        user.account().getNickname(),
        couple.getId(),
        couple.getInviteCode(),
        memberRepository.countByCoupleId(couple.getId())
    );
  }

  @Transactional
  public CoupleDtos.CoupleStatusResponse leave(AuthenticatedUser user) {
    CoupleMember member = memberRepository.findByUserId(user.id()).orElse(null);
    memberRepository.deleteByUserId(user.id());
    String nickname = user.account() != null ? user.account().getNickname() : "";
    return new CoupleDtos.CoupleStatusResponse(
        user.id(),
        nickname,
        null,
        "",
        0
    );
  }

  public Long requireCoupleId(AuthenticatedUser user) {
    return memberRepository.findByUserId(user.id())
        .map(CoupleMember::getCoupleId)
        .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "NOT_BOUND", "请先绑定情侣空间"));
  }

  private CoupleDtos.CoupleResponse response(Couple couple) {
    return new CoupleDtos.CoupleResponse(
        couple.getId(),
        couple.getInviteCode(),
        memberRepository.countByCoupleId(couple.getId())
    );
  }

  private void addMember(Long coupleId, Long userId, String role) {
    CoupleMember member = new CoupleMember();
    member.setCoupleId(coupleId);
    member.setUserId(userId);
    member.setRole(role);
    memberRepository.save(member);
  }

  private String generateUniqueInviteCode() {
    String code;
    do {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < 6; i += 1) {
        builder.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
      }
      code = builder.toString();
    } while (coupleRepository.existsByInviteCode(code));
    return code;
  }
}
