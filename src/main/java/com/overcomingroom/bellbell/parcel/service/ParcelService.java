package com.overcomingroom.bellbell.parcel.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
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
        settingsSchedule(getTrackingProgress(dto, parcel), parcel);

        return ResponseCode.PARCEL_NOTIFICATION_CREATE_SUCCESSFUL;
    }

    /**
     * 사용자가 생성한 택배 알림 정보를 갱신하는 스케줄을 설정합니다.
     *
     */
    private void settingsSchedule(List<TrackingInfoDto> trackingInfoDtos, Parcel parcel) {
        // cronExpression
        String cronExpression = "0 * * * * ?";
        // 예약된 작업 실행
        taskScheduler.schedule(() ->
                updateTrackingInfo(trackingInfoDtos, parcel)
            , new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul")));
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
        return ResponseCode.USER_NOTIFICATION_DELETE_SUCCESSFUL;
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
    public void updateTrackingInfo(List<TrackingInfoDto> trackingInfoDtos, Parcel parcel) {
        if (trackingInfoDtos.size() != getTrackingInfo(parcel).size()) {
            TrackingInfoDto trackingInfoDto = new TrackingInfoDto();
            // DB 저장
            trackingInfoRepository.save(trackingInfoDto.toEntity(trackingInfoDtos.get(trackingInfoDtos.size() - 1).getDescription(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getLocation(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getStatus(), trackingInfoDtos.get(trackingInfoDtos.size() - 1).getTime(), parcel));
            // 카카오 메시지 전송
            log.info("택배 정보 갱신");
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
