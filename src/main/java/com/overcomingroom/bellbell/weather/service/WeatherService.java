package com.overcomingroom.bellbell.weather.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.repository.MemberRepository;
import com.overcomingroom.bellbell.weather.domain.CategoryType;
import com.overcomingroom.bellbell.weather.domain.dto.LocationDto;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherResponse;
import com.overcomingroom.bellbell.weather.domain.entity.Location;
import com.overcomingroom.bellbell.weather.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    @Value("${weather.api-key}")
    private String serviceKey;

    @Value("${weather.service-url}")
    private String weatherApiUrl;

    @Value("${location.service-url}")
    private String locationApiUrl;

    private final MemberRepository memberRepository;

    private final LocationRepository locationRepository;

    // 사용자 위치 정보 등록
    // 사용자가 위치와 x, y 를 저장함.
    public void locationSave(String email, String si, String gu, String dong) {

        // 1. 사용자 정보 찾기
        Member member = findMemberByEmail(email);

        // 2. x, y 지표를 저장

        // 만약 해당 유저가 저장한 지역 정보가 있다면 삭제
        if (chkLocationData(member)) {
            deleteLocationData(member.getId());
        }

        // 저장
        locationRepository.save(Location.toEntity(positionConversion(member.getId(), si, gu, dong)));
    }

    // 멤버 찾기
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_INVALID));
    }

    // 위치 기 저장 여부 확인
    private boolean chkLocationData(Member member) {

        Optional<Location> byMemberId = locationRepository.findByMemberId(member.getId());

        if (byMemberId.isPresent()) {
            return true; // 저장 됨.
        }
        return false; // 저장 되지 않음.
    }

    // 위치 정보 삭제
    private void deleteLocationData(Long memberId) {
        Location location = locationRepository.findByMemberId(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_LOCATION_INFORMATION_NOT_FOUND));
        locationRepository.delete(location);

    }

    // 위도 경도를 날씨 데이터 이용을 위해 x, y 좌표로 변환
    // 이후 dto 로 반환
    private LocationDto positionConversion(Long memberId, String si, String gu, String dong) {

        LocationDto xyCode = findXYCode(si, gu, dong);
        StringBuilder sb = new StringBuilder();
        sb.append(si).append(" ").append(gu).append(" ").append(dong);

        return LocationDto.builder()
                .memberId(memberId)
                .location(sb.toString())
                .gridX(xyCode.getGridX())
                .gridY(xyCode.getGridY())
                .build();
    }


    // 날씨 api 호출
    public WeatherResponse callForecastApi(String email) {
        Member member = findMemberByEmail(email);
        Location location = locationRepository.findByMemberId(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.LOCATION_INFORMATION_NOT_FOUND));

        LocalDate localDate = LocalDate.now();
        String baseDate = localDate.toString().replaceAll("-", "");
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        String baseTime;
        if (minute < 30) {
            baseTime = String.format("%02d%02d", hour, 0);
        } else {
            baseTime = String.format("%02d%02d", hour, 30);
        }
        String numOfRows = "100";
        String pageNo = "1";

        String dataType = "json";
        String nx = location.getGridX();
        String ny = location.getGridY();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherApiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .queryParam("dataType", dataType);

        log.info(">>>>>>>>>>>>> start reading OpenAPI >>>>>>>>>>>>>");
        ResponseEntity<String> response = getResponse(builder);
        log.info("response = {} ", response);
        JSONObject jsonObject = new JSONObject(response.getBody()).getJSONObject("response").getJSONObject("body").getJSONObject("items");
        JSONArray jsonArray = jsonObject.getJSONArray("item");
        Map<CategoryType, String> weatherInfo = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonData = jsonArray.getJSONObject(i);
            weatherInfo.put(CategoryType.valueOf(jsonData.get("category").toString()), jsonData.get("fcstValue").toString());
        }

        if (!response.getBody().startsWith("{")) {
            log.warn("예상치 못한 응답 형식: {}", response.getBody());
            new CustomException(ErrorCode.API_CALL_BAD_REQUEST);
        }
        return new WeatherResponse(weatherInfo);
    }

    // 시 api 정보
    // http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt
    public String findSiCode(String si) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(locationApiUrl + "/top.json.txt");

        ResponseEntity<String> response = getResponse(builder);

        return getCode(si, response.getBody(), ErrorCode.SI_API_CALL_BAD_REQUEST);

    }

    // api 호출
    private ResponseEntity<String> getResponse(UriComponentsBuilder builder) {
        RestTemplate restTemplate = new RestTemplate();
        // UTF-8 설정
        charSetUTF8(restTemplate);
        ResponseEntity<String> response = restTemplate.exchange(builder.build(true).toUri(), HttpMethod.GET, null, String.class);
        return response;
    }

    // utf-8 설정
    private void charSetUTF8(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        HttpMessageConverter<?> converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
    }

    // 구 api 정보
    private String findGuCode(String siCode, String gu) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(locationApiUrl + "/mdl." + siCode + ".json.txt");

        ResponseEntity<String> response = getResponse(builder);
        return getCode(gu, response.getBody(), ErrorCode.GU_API_CALL_BAD_REQUEST);
    }

    private String getCode(String gu, String responseBody, ErrorCode errorCode) {

        JSONArray jsonArray = new JSONArray(responseBody);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String code = obj.getString("code");
            String value = obj.getString("value");
            if (value.equals(gu)) {
                log.info("Code: " + code);
                log.info("Value: " + value);
                return code;
            }
        }

        throw new CustomException(errorCode);
    }

    // 동 api 정보 와 x y 좌표
    private LocationDto findXYCode(String si, String gu, String dong) {

        String siCode = findSiCode(si);
        String guCode = findGuCode(siCode, gu);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(locationApiUrl + "/leaf." + guCode + ".json.txt");

        ResponseEntity<String> response = getResponse(builder);

        JSONArray jsonArray = new JSONArray(response.getBody());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String x = obj.getString("x");
            String y = obj.getString("y");
            String value = obj.getString("value");
            if (value.equals(dong)) {
                log.info("x: " + x);
                log.info("y: " + y);
                log.info("Value: " + value);
                return LocationDto.builder()
                        .gridX(x.toString())
                        .gridY(y.toString())
                        .build(); // x, y를 리턴해야함.
            }
        }
        throw new CustomException(ErrorCode.DONG_API_CALL_BAD_REQUEST);
    }

    // 옷차림 정보

}
