package com.overcomingroom.bellbell.exception;

public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMsg());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return "CustomException{" +
        "errorCode=" + errorCode +
        '}';
  }
}
