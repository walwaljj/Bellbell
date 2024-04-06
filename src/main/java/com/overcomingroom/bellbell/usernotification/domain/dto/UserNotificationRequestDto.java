package com.overcomingroom.bellbell.usernotification.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 사용자 알림 생성 요청을 위한 데이터 전송 객체입니다.
 */
@Getter
public class UserNotificationRequestDto {

  /**
   * 알림 내용을 나타냅니다.
   */
  @NotBlank(message = "알림 내용은 필수 입력값입니다.")
  private String content;

  /**
   * 알림 요일을 나타냅니다.
   */
  @NotBlank(message = "알림 요일은 필수 입력값입니다.")
  private String day;

  /**
   * 알림 시간을 나타냅니다.
   */
  @NotBlank(message = "알림 시간은 필수 입력값입니다.")
  private String time;

}
