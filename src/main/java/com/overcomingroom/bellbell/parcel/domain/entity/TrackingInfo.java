package com.overcomingroom.bellbell.parcel.domain.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrackingInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tracking_info_id")
  private Long id;

  private String status;
  private String location;
  private String description;
  private LocalDateTime time;

  // 택배알림 (N:1)
  @ManyToOne(fetch = LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "parcel_id")
  @JsonIgnore
  private Parcel parcel;

  @Builder
  public TrackingInfo(String description, String location, LocalDateTime time, String status, Parcel parcel) {
    this.description = description;
    this.location = location;
    this.time = time;
    this.status = status;
    this.parcel = parcel;
  }
}
