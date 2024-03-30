package com.overcomingroom.bellbell.basicNotification.service;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.basicNotification.repository.BasicNotificationRepository;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherAndClothesDto;
import com.overcomingroom.bellbell.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.TimeZone;

/**
 * 사용자 알림 서비스를 제공하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicNotificationService {

    private final BasicNotificationRepository basicNotificationRepository;
    private final TaskScheduler taskScheduler;
    private final WeatherService weatherService;

    // 초기 설정
    public BasicNotification setNotification() {
        return basicNotificationRepository.save(new BasicNotification());
    }

    // 초기 이후 설정 변경
    public BasicNotification activeNotification(String className, String accessToken, BasicNotification basicNotification, AbstractBasicNotificationDto basicNotificationDto) {
        basicNotification.setIsActivated(basicNotificationDto.getIsActivated());
        basicNotification.setDay(basicNotificationDto.getDay());
        basicNotification.setTime(basicNotificationDto.getTime());

        BasicNotification saveBasicNotification = basicNotificationRepository.save(basicNotification);
        // 만약 설정된 알람이 활성화 상태라면 스케줄러 세팅
        if (basicNotification.getIsActivated())
            settingsSchedule(getClassName(className), basicNotification, accessToken);

        return saveBasicNotification;
    }

    public Optional<BasicNotification> getNotification(Long id) {
        return basicNotificationRepository.findById(id);
    }

    private String getClassName(String className) {
        String[] split = className.split("\\.");
        return split[split.length - 1];
    }

    private void settingsSchedule(String className, BasicNotification basicNotification, String accessToken) {

        // cronExpression
        String cronExpression = "";

        String dayOfWeek = basicNotification.getDay();
        String time = basicNotification.getTime();
        String[] split = time.split(":");
        int hour = Integer.parseInt(split[0]);
        int minute = Integer.parseInt(split[1]);

        log.info("Updating cron expression: 0 {} {} ? * {}", minute, hour, dayOfWeek);
        cronExpression = String.format("0 %d %d ? * %s", minute, hour, dayOfWeek);

        // 예약된 작업 실행
        taskScheduler.schedule(() -> {

            // task 실행부
            switch (className) {
                case "Weather":
                    WeatherAndClothesDto weatherAndClothesDto = weatherService.weatherAndClothesInfo(accessToken);
                    log.info("\n=========================Execution by scheduling!=========================\n {}", WeatherAndClothesDto.weatherAndClothesInfo(weatherAndClothesDto) + "\n===================================END====================================\n");
            }

        }, new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul")));
    }

}
