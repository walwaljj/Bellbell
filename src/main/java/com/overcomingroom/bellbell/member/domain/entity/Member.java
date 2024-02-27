package com.overcomingroom.bellbell.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저의 Entity
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 닉네임
  @Column(nullable = false)
  private String nickname;

  // 이메일
  @Column(nullable = false)
  private String email;

  @Builder
  public Member(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

}
