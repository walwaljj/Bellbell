package com.overcomingroom.bellbell.weather.domain.dto;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class WeatherDto {

    private Long memberId;

    @Setter
    private BasicNotification basicNotification;

    private String address;

    private String gridX;

    private String gridY;

}
