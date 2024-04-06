package com.overcomingroom.bellbell.parcel.domain.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parcel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "parcel_id")
  private Long id;

  @Column(nullable = false)
  private String carrier;

  @Column(nullable = false)
  private String trackingNo;

  // 회원 (N:1)
  @ManyToOne(fetch = LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "member_id")
  @JsonIgnore
  private Member member;

  // 택배 알림 (1:N)
  @OneToMany(fetch = LAZY, mappedBy = "parcel", orphanRemoval = true)
  private List<TrackingInfo> trackingInfos = new ArrayList<>();

  @Builder
  public Parcel(String carrier, String trackingNo, Member member) {
    this.carrier = carrier;
    this.trackingNo = trackingNo;
    this.member = member;
  }
}
