package com.overcomingroom.bellbell.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // Member
  MEMBER_INVALID(HttpStatus.BAD_REQUEST, "멤버 정보가 유효하지 않습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),

  // OAuth
  LOGIN_ERROR(HttpStatus.BAD_REQUEST, "로그인 오류"),

  // Weather
  LOCATION_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "위치 정보를 찾을 수 없습니다."),
  MEMBER_LOCATION_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 기 저장한 위치 정보가 없습니다."),
  API_CALL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 응답 형식입니다."),
  SI_API_CALL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "알맞는 시, 도를 입력해 주세요. ex) 서울특별시, 경상남도.."),
  GU_API_CALL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "알맞는 시, 구, 군을 입력해 주세요. ex)종로구, 사상구, 창원시.."),
  DONG_API_CALL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "알맞는 동, 읍, 면, 리를 입력해 주세요. ex) 종로1가, 괘법동..."),
  WEATHER_API_RES_RESULT_IS_EMPTY(HttpStatus.NOT_FOUND, "날씨 데이터가 존재하지 않습니다.");

  private final HttpStatus status;
  private final String msg;

  ErrorCode(HttpStatus status, String msg) {
    this.status = status;
    this.msg = msg;
  }

}
