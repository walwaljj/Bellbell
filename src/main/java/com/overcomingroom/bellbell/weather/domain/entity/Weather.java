package com.overcomingroom.bellbell.weather.domain.entity;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherDto;
import jakarta.persistence.*;
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
    private String address;
    private String gridX;
    private String gridY;

    @OneToOne
    @JoinColumn(name = "basic_notification_id")
    @Setter
    private BasicNotification basicNotification;

    public static Weather toEntity(WeatherDto weatherDto) {
        return Weather.builder()
                .basicNotification(weatherDto.getBasicNotification())
                .memberId(weatherDto.getMemberId())
                .address(weatherDto.getAddress())
                .gridX(weatherDto.getGridX())
                .gridY(weatherDto.getGridY())
                .build();
    }

}
