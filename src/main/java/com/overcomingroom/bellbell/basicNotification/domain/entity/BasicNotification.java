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
import lombok.Setter;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Setter
    private boolean isActivated = false;
    private String day;
    private String time;

    public static BasicNotification toEntity(BasicNotificationRequestDto basicNotificationRequestDto) {
        return BasicNotification.builder()
                .day(basicNotificationRequestDto.getDay())
                .time(basicNotificationRequestDto.getTime())
                .build();
    }
}
