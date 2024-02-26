package com.overcomingroom.bellbell.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * 공통적으로 반환 될 속성
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResResult<T> {

    ResponseCode responseCode;
    String code;
    String message;
    T data;

    @Override
    public String toString() {
        return "ResResult{" +
                "responseCode=" + responseCode +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
