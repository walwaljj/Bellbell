package com.overcomingroom.bellbell.weather.service;

import com.overcomingroom.bellbell.basicNotification.domain.entity.BasicNotification;
import com.overcomingroom.bellbell.basicNotification.service.BasicNotificationService;
import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.kakaoMessage.service.CustomMessageService;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.schedule.CronExpression;
import com.overcomingroom.bellbell.schedule.ScheduleType;
import com.overcomingroom.bellbell.weather.domain.CategoryType;
import com.overcomingroom.bellbell.weather.domain.dto.*;
import com.overcomingroom.bellbell.weather.domain.entity.Weather;
import com.overcomingroom.bellbell.weather.repository.WeatherRepository;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class WeatherService {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${weather.api-key}")
    private String serviceKey;

    @Value("${weather.service-url}")
    private String weatherApiUrl;

    @Value("${weather.service-url2}")
    private String weatherApiUrl2;

    private final WeatherRepository weatherRepository;

    private final MemberService memberService;

    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();

    private final BasicNotificationService basicNotificationService;

    private final TaskScheduler taskScheduler;

    private final CustomMessageService customMessageService;
    private String baseTime;
    private String baseDate;

    // 날씨 api 호출(초단기예보)
    private WeatherResponse callForecastApi(Weather location) {

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
        log.info(response.getBody());
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
    public WeatherAndClothesDto weatherAndClothesInfo(String accessToken) {

        // 1. 사용자 정보
        Member member = memberService.getMember(accessToken);

        // 2. 사용자 지역 정보
        Weather weather = findLocationByMember(member);

        // 3. 날씨 api 호출
        setDateAndTime(); // 날짜, 시간 세팅
        WeatherResponse weatherResponse = callForecastApi(weather);  // 초단기예보
        WeatherResponse weatherResponse2 = getUltraSrtNcst(weather); // 초단기실황

        // 4. api 호출 결과에 따른 날씨 및 옷차림 안내
        Map<CategoryType, String> weatherInfo = weatherResponse.getWeatherInfo();
        Map<CategoryType, String> weatherInfo2 = weatherResponse2.getWeatherInfo();

        float temp = 0;
        String sky = "";
        for (CategoryType categoryType : weatherInfo.keySet()) {
            if (categoryType.equals(CategoryType.SKY) || categoryType.equals(CategoryType.PTY)) {
                sky = CategoryType.getCodeInfo(categoryType.toString(), weatherInfo.get(categoryType));
            }
        }
        for (CategoryType categoryType : weatherInfo2.keySet()) {
            if (categoryType.equals(CategoryType.T1H)) {
                temp = Float.parseFloat(weatherInfo2.get(categoryType));
            }
        }

        return WeatherAndClothesDto.builder()
                .temp(temp)
                .clothes(clothesRecommendation(temp))
                .now(LocalTime.now())
                .weather(sky)
                .location(weather.getAddress())
                .baseTime(baseTime)
                .fcstDate(baseDate)
                .build();
    }

    // 멤버 지역 정보 찾기
    private Weather findLocationByMember(Member member) {
        Optional<Weather> optionalWeather = weatherRepository.findByMemberId(member.getId());
        if (optionalWeather.isEmpty()) {
            throw new CustomException(ErrorCode.LOCATION_INFORMATION_NOT_FOUND);
        }
        return optionalWeather.get();
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


    // 옷차림 정보
    private String clothesRecommendation(float temp) {

        if (temp <= 4) {
            return "패딩, 두꺼운 코트, 목도리, 기모제품";
        } else if (temp >= 5 && temp <= 8) {
            return "코트, 가죽자켓, 히트텍, 니트, 레깅스";
        } else if (temp >= 9 && temp <= 11) {
            return "자켓, 트렌치 코트, 야상, 니트, 청바지, 스타킹";
        } else if (temp >= 12 && temp <= 16) {
            return "자켓, 가디건, 야상, 면바지, 청바지, 스타킹";
        } else if (temp >= 17 && temp <= 19) {
            return "얇은 니트, 가디건, 맨투맨, 가디건, 청바지";
        } else if (temp >= 20 && temp <= 22) {
            return "긴팔, 얇은 가디건, 면바지, 청바지";
        } else if (temp >= 23 && temp <= 27) {
            return "반팔, 얇은 셔츠, 반바지, 면바지";
        } else {
            return "반팔, 민소매, 반바지, 원피스";
        }
    }

    // 클라이언트에서 전달받은 주소로 경위도를 가져와 기상청 xy 좌표로 변환 후 위치를 저장 후 날씨 알림 활성화
    public void activeWeather(String accessToken, WeatherInfoDto weatherInfoDto) {
        log.info(weatherInfoDto.toString());

        Member member = memberService.getMember(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + clientId);
        RequestEntity<?> requestEntity = RequestEntity.get(
                        "https://dapi.kakao.com/v2/local/search/address.json?query=" + weatherInfoDto.getAddress()).headers(headers)
                .build();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("documents"));
        double lon = Double.parseDouble(jsonArray.getJSONObject(0).get("x").toString());
        double lat = Double.parseDouble(jsonArray.getJSONObject(0).get("y").toString());

        GpsTransfer gpsTransfer = new GpsTransfer();
        gpsTransfer.transfer(lon, lat);

        // 알림 정보 생성
        Weather weather = weatherRepository.findByMemberId(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.WEATHER_API_RES_RESULT_IS_EMPTY));

        weather.setAddress(weatherInfoDto.getAddress());
        weather.setGridX(String.valueOf(gpsTransfer.getX()));
        weather.setGridY(String.valueOf(gpsTransfer.getY()));

        BasicNotification basicNotification = basicNotificationService.activeNotification(weather.getBasicNotification().getId(), weatherInfoDto);
        weather.setBasicNotification(basicNotification);

        weatherRepository.save(weather);

        String scheduleId = ScheduleType.WEATHER.toString() + weather.getId();
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(scheduleId);
        log.info("scheduleId: {}", scheduleId);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            log.info("기존 날씨 및 옷차림 알림 스케줄을 취소 합니다.");
            scheduledFutureMap.remove(scheduleId, scheduledFuture);
        } else {
            // 알림 스케줄 생성
            String cronExpression = CronExpression.getCronExpression(basicNotification.getDay(), basicNotification.getTime());
            scheduledFuture = taskScheduler.schedule(() -> {
                        customMessageService.sendMessage(accessToken, WeatherAndClothesDto.weatherAndClothesInfo(weatherAndClothesInfo(accessToken)));
                        log.info("날씨 및 옷차림 알림 스케줄 실행 완료.");
                    }, new CronTrigger(cronExpression, TimeZone.getTimeZone("Asia/Seoul"))
            );
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

    // 날씨 api 호출(초단기실황)
    private WeatherResponse getUltraSrtNcst(Weather location) {

        String numOfRows = "100";
        String pageNo = "1";
        String dataType = "json";
        String nx = location.getGridX();
        String ny = location.getGridY();

        // uri 생성
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherApiUrl2)
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
        log.info(response.getBody());
        JSONObject jsonObject = new JSONObject(response.getBody()).getJSONObject("response").getJSONObject("body").getJSONObject("items");
        JSONArray jsonArray = jsonObject.getJSONArray("item");
        Map<CategoryType, String> weatherInfo = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonData = jsonArray.getJSONObject(i);
            weatherInfo.put(CategoryType.valueOf(jsonData.get("category").toString()), jsonData.get("obsrValue").toString());
        }

        if (!response.getBody().startsWith("{")) {
            log.warn("예상치 못한 응답 형식: {}", response.getBody());
            new CustomException(ErrorCode.API_CALL_BAD_REQUEST);
        }

        return new WeatherResponse(weatherInfo);
    }

    // api 호출 날짜, 시간을 세팅합니다.
    private void setDateAndTime() {
        LocalDateTime now = LocalDateTime.now();
        int minute = now.getMinute();

        if (minute < 30) {
            now = now.minusHours(1);
            baseTime = String.format("%02d%02d", now.getHour(), 0);
        } else {
            baseTime = String.format("%02d%02d", now.getHour(), 30);
        }

        if (now.getHour() == 0 && minute < 30) {
            baseDate = now.minusDays(1).toLocalDate().toString().replaceAll("-", "");
            baseTime = String.format("%02d%02d", 23, 30);
        } else {
            baseDate = now.toLocalDate().toString().replaceAll("-", "");
        }

        log.info("baseDate = {}", baseDate);
        log.info("baseTime = {}", baseTime);
    }

    // 날씨 알림 생성
    public void setWeather(Member member) {
        weatherRepository.save(Weather.toEntity(
                WeatherDto.builder()
                        .member(member)
                        .basicNotification(basicNotificationService.setNotification())
                        .build())
        );
    }

    // 날씨 알림 가져오기
    public Optional<Weather> getWeather(String accessToken) {
        return weatherRepository.findByMemberId(memberService.getMember(accessToken).getId());
    }

    // 날씨 알림 정보 가져오기
    public WeatherInfoDto getWeatherInfo(String accessToken) {
        Weather weather = getWeather(accessToken).orElseThrow(() -> new CustomException(ErrorCode.WEATHER_API_RES_RESULT_IS_EMPTY));
        BasicNotification basicNotification = basicNotificationService.getNotification(weather.getBasicNotification().getId()).orElseThrow(() -> new CustomException(ErrorCode.BASIC_NOTIFICATION_IS_EMPTY));

        return WeatherInfoDto.builder()
                .address(weather.getAddress())
                .isActivated(basicNotification.getIsActivated())
                .day(basicNotification.getDay())
                .time(basicNotification.getTime())
                .build();
    }
}
