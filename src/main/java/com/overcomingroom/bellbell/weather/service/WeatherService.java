package com.overcomingroom.bellbell.weather.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.repository.MemberRepository;
import com.overcomingroom.bellbell.weather.domain.CategoryType;
import com.overcomingroom.bellbell.weather.domain.dto.LocationDto;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherAndClothesDto;
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
import java.time.LocalDateTime;
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

    private String baseTime;
    private String baseDate;

    // 사용자 위치 정보 등록
    // 사용자가 위치와 x, y 를 저장함.
    public void locationSave(String email, String si, String gu, String dong) {

        // 1. 사용자 정보 찾기
        Member member = findMemberByEmail(email);

        // 2. x, y 지표를 저장

        // 만약 해당 유저가 저장한 지역 정보가 있다면 삭제
        if (chkLocationData(member)) {
            deleteLocationData(member);
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
    private void deleteLocationData(Member member) {
        Location location = findLocationByMember(member);
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
    private WeatherResponse callForecastApi(Location location) {

        LocalDateTime now = LocalDateTime.now();
        int minute = now.getMinute();

        if (minute < 30) {
            now = now.minusHours(1);
            baseTime = String.format("%02d%02d", now.getHour(), 0);
        } else {
            baseTime = String.format("%02d%02d", now.getHour(), 30);
        }

        if (now.getHour() == 0) {
            baseDate = now.minusDays(1).toLocalDate().toString().replaceAll("-", "");
        } else {
            baseDate = now.toLocalDate().toString().replaceAll("-", "");
        }
        String numOfRows = "100";
        String pageNo = "1";
        String dataType = "json";
        String nx = location.getGridX();
        String ny = location.getGridY();

        // uri 생성
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherApiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .queryParam("dataType", dataType);

        // api 호출
        log.info(">>>>>>>>>>>>> start reading OpenAPI >>>>>>>>>>>>>");
        ResponseEntity<String> response = getResponse(builder);
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

    // 옷차림과 날씨 정보 반환
    public WeatherAndClothesDto weatherAndClothesInfo(String email) {

        // 1. 사용자 정보
        Member member = findMemberByEmail(email);

        // 2. 사용자 지역 정보
        Location location = findLocationByMember(member);

        // 3. 날씨 api 호출
        WeatherResponse weatherResponse = callForecastApi(location);

        // 4. api 호출 결과에 따른 날씨 및 옷차림 안내
        Map<CategoryType, String> weatherInfo = weatherResponse.getWeatherInfo();

        int temp = 0;
        String sky = "";
        for (CategoryType categoryType : weatherInfo.keySet()) {
            if (categoryType.equals(CategoryType.T1H)) {
                temp = Integer.parseInt(weatherInfo.get(categoryType));
            }
            if (categoryType.equals(CategoryType.SKY) || categoryType.equals(CategoryType.PTY)) {
                sky = CategoryType.getCodeInfo(categoryType.toString(), weatherInfo.get(categoryType));
            }
        }

        return WeatherAndClothesDto.builder()
                .temp(temp)
                .clothes(clothesRecommendation(temp))
                .location(location.getLocation())
                .now(LocalTime.now())
                .weather(sky)
                .baseTime(baseTime)
                .fcstDate(baseDate)
                .build();
    }

    // 멤버 지역 정보 찾기
    public Location findLocationByMember(Member member) {
        Optional<Location> optionalLocation = locationRepository.findByMemberId(member.getId());
        if (optionalLocation.isEmpty()) {
            throw new CustomException(ErrorCode.LOCATION_INFORMATION_NOT_FOUND);
        }
        return optionalLocation.get();
    }

    // 시 api 정보
    private String findSiCode(String si) {

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

    // uri 에 필요한 code 를 반환하는 메서드
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
    private String clothesRecommendation(int temp) {

        int intTemp = temp;

        if (intTemp <= 4) {
            return "패딩, 두꺼운 코트, 목도리, 기모제품";
        } else if (intTemp >= 5 && intTemp <= 8) {
            return "코트, 가죽자켓, 히트텍, 니트, 레깅스";
        } else if (intTemp >= 9 && intTemp <= 11) {
            return "자켓, 트렌치 코트, 야상, 니트, 청바지, 스타킹";
        } else if (intTemp >= 12 && intTemp <= 16) {
            return "자켓, 가디건, 야상, 면바지, 청바지, 스타킹";
        } else if (intTemp >= 17 && intTemp <= 19) {
            return "얇은 니트, 가디건, 맨투맨, 가디건, 청바지";
        } else if (intTemp >= 20 && intTemp <= 22) {
            return "긴팔, 얇은 가디건, 면바지, 청바지";
        } else if (intTemp >= 23 && intTemp <= 27) {
            return "반팔, 얇은 셔츠, 반바지, 면바지";
        } else {
            return "반팔, 민소매, 반바지, 원피스";
        }
    }
}
