package com.overcomingroom.bellbell.parcel.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.kakaoMessage.service.CustomMessageService;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.parcel.domain.dto.ParcelRequestDto;
import com.overcomingroom.bellbell.parcel.domain.dto.ParcelResponseDto;
import com.overcomingroom.bellbell.parcel.domain.dto.TrackingInfoDto;
import com.overcomingroom.bellbell.parcel.domain.entity.Parcel;
import com.overcomingroom.bellbell.parcel.domain.entity.TrackingInfo;
import com.overcomingroom.bellbell.parcel.repository.ParcelRepository;
import com.overcomingroom.bellbell.parcel.repository.TrackingInfoRepository;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.schedule.ScheduleType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 택배 알림 서비스를 제공하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParcelService {

    private final TaskScheduler taskScheduler;
    private final ParcelRepository parcelRepository;
    private final MemberService memberService;
    private final TrackingInfoRepository trackingInfoRepository;
    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    private final CustomMessageService customMessageService;

    /**
     * 택배 알림을 생성하는 메서드입니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @param dto         알림 생성 요청 DTO
     * @return 응답 코드
     */
    public ResponseCode createParcelNotification(String accessToken, ParcelRequestDto dto) {
        Member member = memberService.getMember(accessToken);
        Parcel parcel = parcelRepository.save(
            new Parcel(dto.getCarrier(), dto.getTrackingNo(), member));
        // 스케줄 설정 메소드
        saveTrackingInfo(getTrackingProgress(dto, parcel), parcel);
        settingsSchedule(getTrackingProgress(dto, parcel), parcel, accessToken);

        if (scheduledFutureMap.entrySet().isEmpty()) {
            log.info("스케줄 목록이 비어있습니다.");
        } else {
            log.info("Schedule List");
            for (Map.Entry<String, ScheduledFuture<?>> entry : scheduledFutureMap.entrySet()) {
                log.info("Schedule ID: " + entry.getKey());
            }
        }

        customMessageService.sendMessage(accessToken, "운송장 번호: " + dto.getTrackingNo() + " 배송 추적 시작");

        return ResponseCode.PARCEL_NOTIFICATION_CREATE_SUCCESSFUL;
    }

    /**
     * 사용자가 생성한 택배 알림 정보를 갱신하는 스케줄을 설정합니다.
     *
     */
    private void settingsSchedule(List<TrackingInfoDto> trackingInfoDtos, Parcel parcel, String accessToken) {
        // cronExpression
        String cronExpression = "0 * * * * ?";
        // 예약된 작업 실행
        ScheduledFuture<?> schedule = taskScheduler.schedule(() ->
                updateTrackingInfo(trackingInfoDtos, parcel, accessToken)
            , new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul")));

        scheduledFutureMap.put(ScheduleType.PARCEL.toString() + parcel.getId(), schedule);
    }

    /**
     * 사용자의 모든 택배 알림을 가져오는 메서드입니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @return 사용자 택배 알림 목록 DTO
     */
    public List<ParcelResponseDto> getParcelNotifications(String accessToken) {
        Member member = memberService.getMember(accessToken);
        List<Parcel> parcels = parcelRepository.findAllByMember(member)
            .orElseThrow(() -> new CustomException(
                ErrorCode.NO_EXISTS_PARCEL_NOTIFICATION));
        List<ParcelResponseDto> parcelResponseDtos = new ArrayList<>();
        for (Parcel parcel : parcels) {
            ParcelResponseDto dto = new ParcelResponseDto();
            dto.setId(parcel.getId());
            dto.setCarrier(parcel.getCarrier());
            dto.setTrackingNo(parcel.getTrackingNo());
            dto.setTrackingInfoList(getTrackingInfo(parcel));
            parcelResponseDtos.add(dto);
        }
        return parcelResponseDtos;
    }

    /**
     * 사용자의 특정 택배 알림을 삭제하는 메서드입니다.
     *
     * @param accessToken    사용자의 액세스 토큰
     * @param parcelId 삭제할 알림의 ID
     * @return 응답 코드
     */
    public ResponseCode deleteParcelNotification(String accessToken, Long parcelId) {
        Member member = memberService.getMember(accessToken);
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new CustomException(ErrorCode.NO_EXISTS_PARCEL_NOTIFICATION));
        if (!parcel.getMember().equals(member)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        parcelRepository.delete(parcel);

        scheduleCancel(parcelId);

        return ResponseCode.USER_NOTIFICATION_DELETE_SUCCESSFUL;
    }

    private void scheduleCancel(Long parcelId) {
        String scheduleId = ScheduleType.PARCEL.toString() + parcelId;
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(scheduleId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info("\n택배 알림 취소 완료\n parcelId = {}\n scheduledId = {}", parcelId, scheduleId);
            scheduledFutureMap.remove(scheduleId, scheduledFuture);
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
     * 택배 추적 결과를 가져옵니다.
     *
     * @param parcelRequestDto    사용자의 액세스 토큰
     * @param parcel 추적할 택배
     * @return 추적 결과가 담긴 TrackingInfoDto List
     */
    public List<TrackingInfoDto> getTrackingProgress(ParcelRequestDto parcelRequestDto, Parcel parcel) {

        String apiUrl =
            "https://apis.tracker.delivery/carriers/" + parcelRequestDto.getCarrier() + "/tracks/"
                + parcelRequestDto.getTrackingNo();

        // API 호출 및 데이터 가져오기
        RequestEntity<?> requestEntity = RequestEntity.get(apiUrl).build();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            parcelRepository.delete(parcel);
            throw new CustomException(ErrorCode.INVALID_PARCEL_TRACKING_INFO);
        }

        List<TrackingInfoDto> trackingInfoDtos = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response.getBody());
        log.info("jsonObject: {}", jsonObject);
        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("progresses"));
        log.info("jsonArray: {}", jsonArray);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject arrayObject = jsonArray.getJSONObject(i);

            log.info("arrayObject: {}", arrayObject);

            String description = arrayObject.getString("description");
            String location = arrayObject.getJSONObject("location").getString("name");
            LocalDateTime time = LocalDateTime.parse(arrayObject.getString("time")
                .substring(0, arrayObject.get("time").toString().indexOf('+')));
            String status = arrayObject.getJSONObject("status").getString("text");

            log.info("description: {}", description);
            log.info("name: {}", location);
            log.info("time: {}", time);
            log.info("text: {}", status);
            trackingInfoDtos.add(new TrackingInfoDto(description, location, status, time, parcel));
        }
        return trackingInfoDtos;
    }

    // 추적 변동 사항이 있으면 갱신합니다.
    public void updateTrackingInfo(List<TrackingInfoDto> trackingInfoDtos, Parcel parcel, String accessToken) {
        log.info("accessToken: {}", accessToken);
        List<TrackingInfo> trackingInfos = getTrackingInfo(parcel);
        if (trackingInfoDtos.size() != trackingInfos.size()) {
            TrackingInfoDto trackingInfoDto = new TrackingInfoDto();
            // DB 저장
            trackingInfoRepository.save(trackingInfoDto.toEntity(trackingInfoDtos.get(trackingInfoDtos.size() - 1).getDescription(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getLocation(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getStatus(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getTime(), parcel));
            // 카카오 메시지 전송
            log.info("택배 정보 갱신");
            StringBuilder sb = new StringBuilder();
            sb.append("운송장 번호: " + parcel.getTrackingNo() + "의 배송 상태가 업데이트 되었습니다.\n")
                .append("시간: " + trackingInfoDto.getTime().toString().replace('T', ' ').substring(0, trackingInfoDto.getTime().toString().length() - 3) + "\n")
                .append("상태: " + trackingInfoDto.getStatus() + "\n")
                .append("위치: " + trackingInfoDto.getLocation() + "\n")
                .append("상세: " + trackingInfoDto.getDescription() + "\n");
            log.info(sb.toString());
            customMessageService.sendMessage(accessToken, sb.toString());
        }
        if (trackingInfos.get(trackingInfos.size() - 1).getStatus().equals("배송완료")) {
            log.info("배송 완료 건 스케줄 취소");
            scheduleCancel(parcel.getId());
            parcelRepository.deleteById(parcel.getId());
            customMessageService.sendMessage(accessToken, "운송장 번호: " + parcel.getTrackingNo() + " 배송이 완료되어 목록에서 삭제되었습니다.");
        }
    }

    // 추적 결과를 저장합니다.
    public void saveTrackingInfo(List<TrackingInfoDto> trackingInfoDtos, Parcel parcel) {
        for (TrackingInfoDto trackingInfoDto : trackingInfoDtos) {
            trackingInfoRepository.save(new TrackingInfoDto().toEntity(trackingInfoDto.getDescription(), trackingInfoDto.getLocation(), trackingInfoDto.getStatus(), trackingInfoDto.getTime(), parcel));
        }
    }

    // DB 에 저장된 운송장 정보를 가져옵니다.
    public List<TrackingInfo> getTrackingInfo(Parcel parcel) {
        return trackingInfoRepository.findAllByParcel(parcel);
    }

}
