package com.overcomingroom.bellbell.usernotification.domain.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.overcomingroom.bellbell.member.domain.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_notification_id")
  private Long id;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private String time;

  @Column(nullable = false)
  private String day;

  // 회원 (N:1)
  @ManyToOne(fetch = LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "member_id")
  private Member member;

  @Builder
  public UserNotification(String content, String time, String day, Member member) {
    this.content = content;
    this.time = time;
    this.day = day;
    this.member = member;
  }

}
