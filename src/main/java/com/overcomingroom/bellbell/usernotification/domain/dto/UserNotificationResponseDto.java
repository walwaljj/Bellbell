package com.overcomingroom.bellbell.usernotification.domain.dto;

import lombok.Data;

/**
 * 사용자 알림 조회 응답을 위한 데이터 전송 객체입니다.
 */
@Data
public class UserNotificationResponseDto {

  /**
   * 알림의 고유 식별자를 나타냅니다.
   */
  private Long id;

  /**
   * 알림 내용을 나타냅니다.
   */
  private String content;

  /**
   * 알림 요일을 나타냅니다.
   */
  private String day;

  /**
   * 알림 시간을 나타냅니다.
   */
  private String time;

}
