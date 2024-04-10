package com.overcomingroom.bellbell.lunch.service;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.basicNotification.service.BasicNotificationService;
import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.kakaoMessage.service.CustomMessageService;
import com.overcomingroom.bellbell.lunch.domain.dto.LunchRequestDto;
import com.overcomingroom.bellbell.lunch.domain.dto.LunchResponseDto;
import com.overcomingroom.bellbell.lunch.domain.entity.Lunch;
import com.overcomingroom.bellbell.lunch.domain.entity.Menu;
import com.overcomingroom.bellbell.lunch.repository.LunchRepository;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.schedule.CronExpression;
import com.overcomingroom.bellbell.schedule.ScheduleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class LunchService {

    private final MenuService menuService;
    private final MemberService memberService;
    private final LunchRepository lunchRepository;
    private final BasicNotificationService basicNotificationService;
    private final TaskScheduler taskScheduler;
    private final CustomMessageService customMessageService;
    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();

    /**
     * lunch service 를 활성 / 비활성 합니다.
     *
     * @param accessToken
     * @param lunchRequestDto
     */
    public void activeLunch(String accessToken, LunchRequestDto lunchRequestDto) {

        Member member = memberService.getMember(accessToken);
        List<Menu> menuList = menuService.recommendMenus();
        LunchResponseDto lunchResponseDto = new LunchResponseDto(lunchRequestDto.getIsActivated(), lunchRequestDto.getDay(), lunchRequestDto.getTime(), menuList);

        // 알림 정보 생성
        Lunch lunch = lunchRepository.findByMember(member).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_IN_LUNCH));

        BasicNotification basicNotification = basicNotificationService.activeNotification(lunch.getBasicNotification().getId(), lunchRequestDto);
        lunch.setBasicNotification(basicNotification);
        lunchRepository.save(lunch);

        String scheduleId = ScheduleType.LUNCH.toString() + lunch.getId();
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(scheduleId);

        // 알림 스케줄 생성
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info("기존 기본 알림 스케줄을 취소 합니다.");
            scheduledFutureMap.remove(scheduleId, scheduledFuture);
        } else {
            String cronExpression = CronExpression.getCronExpression(basicNotification.getDay(), basicNotification.getTime());
            log.info(" {} 상태로 스케줄이 활성화 되었습니다.", basicNotification.getIsActivated());
            scheduledFuture = taskScheduler.schedule(() -> {
                        customMessageService.sendMessage(accessToken, lunchResponseDto.toString());
                        log.info("점심 메뉴 추천 스케줄 실행 완료.");
                    }
                    , new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul")));
            scheduledFutureMap.put(scheduleId, scheduledFuture);
        }

        if (scheduledFutureMap.entrySet().isEmpty()) {
            log.info("스케줄 목록이 비어있습니다.");
        } else {
            log.info("Schedule List");
            for (Map.Entry<String, ScheduledFuture<?>> entry : scheduledFutureMap.entrySet()) {
                log.info("Schedule ID: " + entry.getKey());
            }
        }
    }

    /**
     * member 정보로 Lunch 를 찾습니다.
     *
     * @param member 멤버 정보
     * @return Optional<Lunch>
     */
    public Optional<Lunch> getLunchByMember(Member member) {
        return lunchRepository.findByMember(member);
    }

    /**
     * member의 초기 Lunch 정보를 설정합니다.
     *
     * @param member 멤버 정보
     */
    public void setLunch(Member member) {
        lunchRepository.save(
                Lunch.builder()
                        .member(member)
                        .basicNotification(basicNotificationService.setNotification())
                        .build());
    }

    public LunchResponseDto getLunchInfo(String accessToken) {
        Lunch lunch = getLunchByMember(memberService.getMember(accessToken)).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_LUNCH_INFO));
        BasicNotification basicNotification = basicNotificationService.getNotification(lunch.getBasicNotification().getId()).orElseThrow(() -> new CustomException(ErrorCode.BASIC_NOTIFICATION_IS_EMPTY));

        return LunchResponseDto.builder()
                .isActivated(basicNotification.getIsActivated())
                .day(basicNotification.getDay())
                .time(basicNotification.getTime())
                .build();
    }
}
