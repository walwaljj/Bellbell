package com.overcomingroom.bellbell.weather.controller;

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
        @RequestHeader("Authorization") String accessToken,
        @RequestParam String address
    ) {
        weatherService.saveLocationWithAddress(accessToken.substring(7), address);
        ResponseCode responseCode = ResponseCode.MEMBER_LOCATION_SAVE_SUCCESSFUL;

        return ResponseEntity.ok(
            ResResult.builder()
                .responseCode(responseCode)
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build());
    }
}
