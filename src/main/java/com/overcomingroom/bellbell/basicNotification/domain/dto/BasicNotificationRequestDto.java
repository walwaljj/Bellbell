package com.overcomingroom.bellbell.basicNotification.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicNotificationRequestDto {

    private boolean isActivated;
    private String day;
    private String time;

}
