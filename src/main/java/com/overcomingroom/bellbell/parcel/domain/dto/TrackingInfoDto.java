package com.overcomingroom.bellbell.parcel.domain.dto;

import com.overcomingroom.bellbell.parcel.domain.entity.Parcel;
import com.overcomingroom.bellbell.parcel.domain.entity.TrackingInfo;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrackingInfoDto {
  private String status;
  private String location;
  private String description;
  private LocalDateTime time;
  private Parcel parcel;

  public TrackingInfoDto(String description, String location, String status, LocalDateTime time, Parcel parcel) {
    this.description = description;
    this.location = location;
    this.status = status;
    this.time = time;
    this.parcel = parcel;
  }

  @Builder
  public TrackingInfo toEntity(String description, String location, String status, LocalDateTime time, Parcel parcel) {
    return TrackingInfo.builder()
        .description(description)
        .status(status)
        .location(location)
        .time(time)
        .parcel(parcel)
        .build();
  }
}
