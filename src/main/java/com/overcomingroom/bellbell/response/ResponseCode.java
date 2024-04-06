package com.overcomingroom.bellbell.response;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@ToString
public enum ResponseCode {
  // Member
  MEMBER_INFO_GET_SUCCESSFUL(HttpStatus.OK, "200", "멤버 정보 조회 성공"),

  // OAuth
  LOGIN_SUCCESSFUL(HttpStatus.OK, "200", "로그인 성공"),

  // Weather
  WEATHER_INFO_GET_SUCCESSFUL(HttpStatus.OK, "200" ,"날씨 정보 조회 성공" ),
  MEMBER_LOCATION_SAVE_SUCCESSFUL(HttpStatus.OK, "200" ,"위치 정보 저장 성공" ),
  WEATHER_ACTIVATE_SUCCESSFUL(HttpStatus.OK, "200", "날씨 알림 활성화 성공"),
  WEATHER_INACTIVATE_SUCCESSFUL(HttpStatus.OK, "200", "날씨 알림 비활성화 성공"),

  // Parcel
  PARCEL_INFO_GET_SUCCESSFUL(HttpStatus.OK, "200", "택배 알림 목록 가져오기 성공"),
  PARCEL_NOTIFICATION_CREATE_SUCCESSFUL(HttpStatus.OK, "200", "택배 알림 생성 성공"),

  // UserNotification
  USER_NOTIFICATION_CREATE_SUCCESSFUL(HttpStatus.OK, "200", "사용자 알림 생성 성공"),
  USER_NOTIFICATIONS_GET_SUCCESSFUL(HttpStatus.OK, "200", "사용자 알림 목록 가져오기 성공"),
  USER_NOTIFICATION_DELETE_SUCCESSFUL(HttpStatus.OK, "200", "사용자 알림 삭제 성공");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  ResponseCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public ResponseEntity<ResResult> toResponse(Object data) {
    return new ResponseEntity<>(ResResult.builder()
        .responseCode(this)
        .code(this.code)
        .message(this.message)
        .data(data)
        .build(), httpStatus.OK);
  }
}
