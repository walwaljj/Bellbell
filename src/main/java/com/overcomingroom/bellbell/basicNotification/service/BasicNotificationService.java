package com.overcomingroom.bellbell.basicNotification.service;

import com.overcomingroom.bellbell.basicNotification.domain.dto.BasicNotificationRequestDto;
import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.basicNotification.repository.BasicNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 알림 서비스를 제공하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicNotificationService {

    private final BasicNotificationRepository basicNotificationRepository;

    public BasicNotification setNotification() {
        return basicNotificationRepository.save(new BasicNotification());
    }

    public BasicNotification activeNotification(BasicNotificationRequestDto basicNotificationRequestDto) {
        BasicNotification basicNotification = BasicNotification.toEntity(basicNotificationRequestDto);
        basicNotification.setActivated(basicNotificationRequestDto.isActivated());
        return basicNotificationRepository.save(basicNotification);
    }

}
