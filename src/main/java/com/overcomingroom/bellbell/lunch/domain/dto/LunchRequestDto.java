package com.overcomingroom.bellbell.lunch.domain.dto;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
import lombok.Getter;

@Getter
public class LunchRequestDto extends AbstractBasicNotificationDto {
    protected LunchRequestDto(Boolean isActivated, String day, String time) {
        super(isActivated, day, time);
    }
}
