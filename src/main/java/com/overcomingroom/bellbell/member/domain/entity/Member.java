package com.overcomingroom.bellbell.member.domain.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.overcomingroom.bellbell.usernotification.domain.entity.UserNotification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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
  @Column(name = "member_id")
  private Long id;

  // 닉네임
  @Column(nullable = false)
  private String nickname;

  // 이메일
  @Column(nullable = false)
  private String email;

  // 사용자 생성 알림 (1:N)
  @OneToMany(fetch = LAZY, mappedBy = "member", orphanRemoval = true)
  private List<UserNotification> userNotifications = new ArrayList<>();

  @Builder
  public Member(String nickname, String email, List<UserNotification> userNotifications) {
    this.nickname = nickname;
    this.email = email;
    this.userNotifications = userNotifications;
  }

}
