package com.overcomingroom.bellbell.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Weather
    LOCATION_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "위치 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String msg;

    ErrorCode(HttpStatus status, String msg) {
        this.status = status;
        this.msg = msg;
    }

}
