package com.overcomingroom.bellbell.basicNotification.domain.entity;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
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
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Boolean isActivated = false;
    private String day;
    private String time;

    public static BasicNotification toEntity(
        AbstractBasicNotificationDto abstractBasicNotificationDto) {
        return BasicNotification.builder()
                .isActivated(abstractBasicNotificationDto.getIsActivated())
                .day(abstractBasicNotificationDto.getDay())
                .time(abstractBasicNotificationDto.getTime())
                .build();
    }
}
