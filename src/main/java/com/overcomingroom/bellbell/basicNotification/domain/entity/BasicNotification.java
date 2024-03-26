package com.overcomingroom.bellbell.basicNotification.domain.entity;

import com.overcomingroom.bellbell.basicNotification.domain.dto.BasicNotificationRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean isActivated;
    private String day;
    private String time;

    public static BasicNotification toEntity(BasicNotificationRequestDto basicNotificationRequestDto) {
        return BasicNotification.builder()
                .isActivated(basicNotificationRequestDto.isActivated())
                .day(basicNotificationRequestDto.getDay())
                .time(basicNotificationRequestDto.getTime())
                .build();
    }
}
