package com.overcomingroom.bellbell.weather.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherDto {

    private Long memberId;

    private Long basicNotificationId;

    private String address;

    private String gridX;

    private String gridY;

}
