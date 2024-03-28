package com.overcomingroom.bellbell.weather.domain.dto;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class WeatherDto {

    @Setter
    private Member member;

    @Setter
    private BasicNotification basicNotification;

    private String address;

    private String gridX;

    private String gridY;

}
