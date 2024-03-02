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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    @Value("${weather.api-key}")
    private String serviceKey;

    @Value("${weather.service-url}")
    private String apiUrl;

    private final MemberRepository memberRepository;

    private final LocationRepository locationRepository;

    // 사용자 위치 정보 등록
    // 사용자가 위치와 x, y 를 저장함.
    public void locationSave(String nickname, String location) {

        // 1. 사용자 정보 찾기
        Member member = memberRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_INVALID));

        // 2. x, y 지표를 저장
        locationRepository.save(Location.toEntity(positionConversion(location)));
    }


    // 위도 경도를 날씨 데이터 이용을 위해 x, y 좌표로 변환
    private LocationDto positionConversion(String location) {

        // 2. 해당 위치의 x, y 지표를 구함.

        return LocationDto.builder()
                .location(location)
//                .gridX()
//                .gridY()
                .build();
    }


    // 날씨 api 호출
    public WeatherResponse callForecastApi() {

        LocalDate now = LocalDate.now();
        String baseDate = now.toString().replaceAll("-", "");
        String numOfRows = "3";
        String pageNo = "1";
        String baseTime = "0500";
        String dataType = "json";
        String nx = "55"; // 사용자 입력으로 변경 예정
        String ny = "127"; // 사용자 입력으로 변경 예정

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .queryParam("dataType", dataType);

        RestTemplate restTemplate = new RestTemplate();

        log.info(">>>>>>>>>>>>> start reading OpenAPI >>>>>>>>>>>>>");
        ResponseEntity<String> response = restTemplate.exchange(builder.build(true).toUri(), HttpMethod.GET, null, String.class);
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
    // 옷차림 정보

}
