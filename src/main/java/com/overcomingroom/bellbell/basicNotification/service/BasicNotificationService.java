package com.overcomingroom.bellbell.basicNotification.service;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.basicNotification.repository.BasicNotificationRepository;
import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 알림 서비스를 제공하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicNotificationService {

    private final BasicNotificationRepository basicNotificationRepository;

    // 초기 설정
    public BasicNotification setNotification() {
        return basicNotificationRepository.save(new BasicNotification());
    }

    public BasicNotification activeNotification(Long basicNotificationId, AbstractBasicNotificationDto basicNotificationDto) {
        BasicNotification basicNotification = basicNotificationRepository.findById(basicNotificationId).orElseThrow(() -> new CustomException(
            ErrorCode.BASIC_NOTIFICATION_IS_EMPTY));
        basicNotification.setIsActivated(basicNotificationDto.getIsActivated());
        basicNotification.setDay(basicNotificationDto.getDay());
        basicNotification.setTime(basicNotificationDto.getTime());
        return basicNotificationRepository.save(basicNotification);
    }

    public Optional<BasicNotification> getNotification(Long id) {
        return basicNotificationRepository.findById(id);
    }

}
