package com.overcomingroom.bellbell.weather.domain.dto;

import com.overcomingroom.bellbell.weather.domain.entity.Location;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationDto {

    private Long memberId;

    private String location;

    private String gridX;

    private String gridY;

    public static LocationDto of(Location location) {
        return LocationDto.builder()
                .memberId(location.getMemberId())
                .location(location.getLocation())
                .gridX(location.getGridX())
                .gridY(location.getGridY())
                .build();
    }
}
