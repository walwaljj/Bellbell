package com.overcomingroom.bellbell.weather.controller;

import com.overcomingroom.bellbell.resolver.Login;
import com.overcomingroom.bellbell.resolver.LoginUser;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherInfoDto;
import com.overcomingroom.bellbell.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<ResResult> weatherAndClothesInfo(@Parameter(hidden = true) @Login LoginUser loginUser) {

        ResponseCode responseCode = ResponseCode.WEATHER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(weatherService.weatherAndClothesInfo(loginUser.getAccessToken()))
                        .build());
    }

    @PostMapping("/weather")
    public ResponseEntity<ResResult> activateWeather(
            @Parameter(hidden = true) @Login LoginUser loginUser,
            @RequestBody WeatherInfoDto weatherInfoDto
    ) {

        weatherService.activeWeather(loginUser.getAccessToken(), weatherInfoDto);
        ResponseCode responseCode = ResponseCode.WEATHER_ACTIVATE_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build());
    }

    @GetMapping("/weather")
    public ResponseEntity<ResResult> weatherInfo(@Parameter(hidden = true) @Login LoginUser loginUser) {

        ResponseCode responseCode = ResponseCode.WEATHER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(weatherService.getWeatherInfo(loginUser.getAccessToken()))
                        .build());
    }

}
