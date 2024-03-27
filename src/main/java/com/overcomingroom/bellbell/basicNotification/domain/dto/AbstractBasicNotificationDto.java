package com.overcomingroom.bellbell.basicNotification.domain.dto;

import lombok.Getter;

@Getter
public abstract class AbstractBasicNotificationDto {

    private final Boolean isActivated;
    private final String day;
    private final String time;


  protected AbstractBasicNotificationDto(Boolean isActivated, String day, String time) {
    this.isActivated = isActivated;
    this.day = day;
    this.time = time;
  }
}
