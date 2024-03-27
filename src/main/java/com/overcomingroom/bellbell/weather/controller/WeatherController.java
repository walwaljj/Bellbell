package com.overcomingroom.bellbell.weather.controller;

import com.overcomingroom.bellbell.basicNotification.domain.dto.BasicNotificationRequestDto;
import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.interceptor.AuthorizationInterceptor;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/v1", produces = "application/json; charset=utf8")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public ResponseEntity<ResResult> weatherAndClothesInfo(
            @RequestHeader("Authorization") String accessToken
    ) {
        ResponseCode responseCode = ResponseCode.WEATHER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(weatherService.weatherAndClothesInfo(accessToken.substring(7)))
                        .build());
    }

    @PostMapping("/location")
    public ResponseEntity<ResResult> saveLocationWithAddress(
            @RequestParam String address,
            @RequestParam String day,
            @RequestParam String time,
            @RequestParam(defaultValue = "true") String isActivated
    ) {

        String accessToken = AuthorizationInterceptor.getAccessToken();
        if (accessToken == null) {
            // 토큰이 없는 경우 에러 처리
            throw new CustomException(ErrorCode.JWT_VALUE_IS_EMPTY);
        }

        BasicNotificationRequestDto basicNotificationRequestDto = BasicNotificationRequestDto.builder()
                .day(day)
                .time(time)
                .isActivated(Boolean.valueOf(isActivated))
                .build();
        log.info("토큰 = {}", accessToken);
        weatherService.saveLocationWithAddress(accessToken.substring(7), address, basicNotificationRequestDto);
        ResponseCode responseCode = ResponseCode.MEMBER_LOCATION_SAVE_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build());
    }
}
