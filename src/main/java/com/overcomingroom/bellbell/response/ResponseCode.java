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
  LOCATION_INFORMATION_SEARCH_SUCCESSFUL(HttpStatus.OK, "200", "위치 정보 조회 성공"),
  WEATHER_INFO_GET_SUCCESSFUL(HttpStatus.OK, "200" ,"날씨 정보 조회 성공" ),
  MEMBER_LOCATION_SAVE_SUCCESSFUL(HttpStatus.OK, "200" ,"위치 정보 저장 성공" );

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
