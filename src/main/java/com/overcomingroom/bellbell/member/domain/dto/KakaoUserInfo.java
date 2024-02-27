package com.overcomingroom.bellbell.member.domain.dto;

import com.overcomingroom.bellbell.member.domain.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오에서 받아온 사용자 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserInfo {
  private String nickname; // 카카오 프로필 닉네임
  private String email; // 카카오 프로필 이메일

  /**
   * Dto 객체를 Member Entity 로 변환
   *
   * @return Member Entity
   */
  public Member toEntity() {
    return Member.builder()
        .email(this.email)
        .nickname(this.nickname)
        .build();
  }

}
