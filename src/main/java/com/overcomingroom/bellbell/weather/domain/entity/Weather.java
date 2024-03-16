package com.overcomingroom.bellbell.weather.domain.entity;

import com.overcomingroom.bellbell.weather.domain.dto.WeatherDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder(toBuilder = true)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    @Setter
    private Long basicNotificationId;
    private String address;
    private String gridX;
    private String gridY;

    public static Weather toEntity(WeatherDto weatherDto) {
        return Weather.builder()
                .basicNotificationId(weatherDto.getBasicNotificationId())
                .memberId(weatherDto.getMemberId())
                .address(weatherDto.getAddress())
                .gridX(weatherDto.getGridX())
                .gridY(weatherDto.getGridY())
                .build();
    }

}
