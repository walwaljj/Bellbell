package com.overcomingroom.bellbell.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private ErrorCode errorCode;
  private String code;
  private String message;
  private HttpStatus httpStatus;

  public ErrorResponse(ErrorCode errorCode) {
    this.code = errorCode.getStatus().toString();
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getStatus();
    this.message = errorCode.getMsg();
  }
}
