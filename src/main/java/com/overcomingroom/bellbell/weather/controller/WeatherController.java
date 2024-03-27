package com.overcomingroom.bellbell.weather.controller;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.interceptor.AuthorizationInterceptor;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherInfoDto;
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

    @GetMapping("/weatherAndClothes")
    public ResponseEntity<ResResult> weatherAndClothesInfo() {
        String accessToken = AuthorizationInterceptor.getAccessToken();
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
            @RequestBody WeatherInfoDto weatherInfoDto
    ) {
        String accessToken = AuthorizationInterceptor.getAccessToken();

                // 토큰이 없는 경우 예외 처리
        if (accessToken == null) {
            throw new CustomException(ErrorCode.JWT_VALUE_IS_EMPTY);
        }

        log.info("토큰 = {}", accessToken);
        weatherService.saveLocationWithAddress(accessToken.substring(7),
            weatherInfoDto);
        ResponseCode responseCode = ResponseCode.WEATHER_ACTIVATE_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build());
    }

    @GetMapping("/weather")
    public ResponseEntity<ResResult> weatherInfo() {
        String accessToken = AuthorizationInterceptor.getAccessToken();
        // 토큰이 없는 경우 예외 처리
        if (accessToken == null) {
            throw new CustomException(ErrorCode.JWT_VALUE_IS_EMPTY);
        }

        ResponseCode responseCode = ResponseCode.WEATHER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
            ResResult.builder()
                .responseCode(responseCode)
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .data(weatherService.getWeatherInfo(accessToken.substring(7)))
                .build());
    }

}
