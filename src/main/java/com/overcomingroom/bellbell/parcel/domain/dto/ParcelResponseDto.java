package com.overcomingroom.bellbell.parcel.domain.dto;

import com.overcomingroom.bellbell.parcel.domain.entity.TrackingInfo;
import java.util.List;
import lombok.Data;

/**
 * 택배 알림 조회 응답을 위한 데이터 전송 객체입니다.
 */
@Data
public class ParcelResponseDto {

  /**
   * 알림의 고유 식별자를 나타냅니다.
   */
  private Long id;

  /**
   * 택배사를 나타냅니다.
   */
  private String carrier;

  /**
   * 운송장 번호를 나타냅니다.
   */
  private String trackingNo;

  /**
   * 운송 정보를 나타냅니다.
   */
  private List<TrackingInfo> trackingInfoList;

}
