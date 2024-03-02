package com.overcomingroom.bellbell.weather.domain.entity;

import com.overcomingroom.bellbell.weather.domain.dto.LocationDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder(toBuilder = true)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long memberId;

    private String location;

    private String gridX;

    private String gridY;

    public static Location toEntity(LocationDto location) {
        return Location.builder()
                .memberId(location.getMemberId())
                .location(location.getLocation())
                .gridX(location.getGridX())
                .gridY(location.getGridY())
                .build();
    }
}
