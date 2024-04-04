package com.overcomingroom.bellbell.usernotification.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.kakaoMessage.service.CustomMessageService;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.schedule.CronExpression;
import com.overcomingroom.bellbell.usernotification.domain.dto.UserNotificationRequestDto;
import com.overcomingroom.bellbell.usernotification.domain.dto.UserNotificationResponseDto;
import com.overcomingroom.bellbell.usernotification.domain.entity.UserNotification;
import com.overcomingroom.bellbell.usernotification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 사용자 알림 서비스를 제공하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserNotificationService {

    private final TaskScheduler taskScheduler;
    private final UserNotificationRepository userNotificationRepository;
    private final MemberService memberService;
    private final CustomMessageService customMessageService;
    private Map<Long, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();

    /**
     * 사용자 알림을 생성하는 메서드입니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @param dto         알림 생성 요청 DTO
     * @return 응답 코드
     */
    public ResponseCode createUserNotification(String accessToken, UserNotificationRequestDto dto) {
        Member member = memberService.getMember(accessToken);
        UserNotification userNotification = userNotificationRepository.save(new UserNotification(dto.getContent(), dto.getTime(), dto.getDay(), member));

        // 스케줄 설정 메소드
        settingsSchedule(userNotification, accessToken);

        return ResponseCode.USER_NOTIFICATION_CREATE_SUCCESSFUL;
    }

    /**
     * 사용자가 생성한 알림 정보를 토대로 스케줄을 설정합니다.
     *
     * @param userNotification 알림 서비스 정보
     */
    private void settingsSchedule(UserNotification userNotification, String accessToken) {

        // cronExpression
        String cronExpression = CronExpression.getCronExpression(userNotification.getDay(), userNotification.getTime());

        // 예약된 작업 실행
        ScheduledFuture<?> schedule = taskScheduler.schedule(() -> {

            // 유저가 설정한 알림 컨텐츠를 반환함.
            log.info("사용자 알림 = {}\n == END ==\n", userNotification.getContent());
            customMessageService.sendMessage(accessToken, userNotification.getContent());
            log.info("기본 알림 스케줄 실행 완료.");

        }, new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul")));

        scheduledFutureMap.put(userNotification.getId(), schedule);
    }

    /**
     * 사용자의 모든 알림을 가져오는 메서드입니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @return 사용자 알림 목록 DTO
     */
    public List<UserNotificationResponseDto> getUserNotifications(String accessToken) {
        Member member = memberService.getMember(accessToken);
        List<UserNotification> userNotifications = userNotificationRepository.findAllByMember(member)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.NOT_EXISTS_USER_NOTIFICATION));
        List<UserNotificationResponseDto> userNotificationResponseDtos = new ArrayList<>();
        for (UserNotification userNotification : userNotifications) {
            UserNotificationResponseDto dto = new UserNotificationResponseDto();
            dto.setId(userNotification.getId());
            dto.setContent(userNotification.getContent());
            dto.setTime(userNotification.getTime());
            dto.setDay(userNotification.getDay());
            userNotificationResponseDtos.add(dto);
        }
        return userNotificationResponseDtos;
    }

    /**
     * 사용자의 특정 알림을 삭제하는 메서드입니다.
     *
     * @param accessToken    사용자의 액세스 토큰
     * @param notificationId 삭제할 알림의 ID
     * @return 응답 코드
     */
    public ResponseCode deleteUserNotification(String accessToken, Long notificationId) {
        Member member = memberService.getMember(accessToken);
        UserNotification userNotification = userNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_USER_NOTIFICATION));
        if (!userNotification.getMember().equals(member)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        userNotificationRepository.delete(userNotification);
        // 스케줄도 캔슬하기
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(notificationId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info("\n사용자 스케줄 취소 완료\n notificationId = {}\n scheduledId = {}", notificationId, scheduledFutureMap.get(scheduledFuture));
            scheduledFutureMap.remove(notificationId, scheduledFuture);
        }
        return ResponseCode.USER_NOTIFICATION_DELETE_SUCCESSFUL;
    }

}
