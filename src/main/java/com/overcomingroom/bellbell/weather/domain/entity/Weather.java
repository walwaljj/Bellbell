package com.overcomingroom.bellbell.weather.domain.entity;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherDto;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder(toBuilder = true)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;
    private String address;
    private String gridX;
    private String gridY;

    @OneToOne
    @JoinColumn(name = "basic_notification_id")
    private BasicNotification basicNotification;

    public static Weather toEntity(WeatherDto weatherDto) {
        return Weather.builder()
                .basicNotification(weatherDto.getBasicNotification())
                .member(weatherDto.getMember())
                .address(weatherDto.getAddress())
                .gridX(weatherDto.getGridX())
                .gridY(weatherDto.getGridY())
                .build();
    }

}
