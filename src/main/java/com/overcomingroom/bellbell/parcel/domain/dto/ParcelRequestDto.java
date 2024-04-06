package com.overcomingroom.bellbell.parcel.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 택배 알림 생성 요청을 위한 데이터 전송 객체입니다.
 */
@Getter
public class ParcelRequestDto {

  /**
   * 택배사를 나타냅니다.
   */
  @NotBlank(message = "택배사는 필수 입력값입니다.")
  private String carrier;

  /**
   * 운송장 번호을 나타냅니다.
   */
  @NotBlank(message = "운송장 번호는 필수 입력값입니다.")
  private String trackingNo;

}

