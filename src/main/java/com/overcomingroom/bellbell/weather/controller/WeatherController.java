package com.overcomingroom.bellbell.weather.controller;

import com.overcomingroom.bellbell.member.domain.dto.KakaoUserInfo;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/v1",produces = "application/json; charset=utf8")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authentication")
public class WeatherController {

    private final WeatherService weatherService;
    private final MemberService memberService;

    @PostMapping("/location")
    public ResponseEntity<ResResult> saveLocation(
            @RequestHeader("Authentication") String accessToken,
            @RequestParam String si,
            @RequestParam String gu,
            @RequestParam String dong
    ) {

        KakaoUserInfo memberInfo = memberService.getMemberInfo(accessToken);
        weatherService.locationSave(memberInfo.getNickname(), si, gu, dong);

        ResponseCode responseCode = ResponseCode.MEMBER_LOCATION_SAVE_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build());
    }

    @GetMapping("/weather") // API 를 호출하고 JSON을 객체로 저장함.
    public ResponseEntity<ResResult> callForecastApi(
            @RequestHeader("Authentication") String accessToken
    ) {
        KakaoUserInfo memberInfo = memberService.getMemberInfo(accessToken);
        ResponseCode responseCode = ResponseCode.WEATHER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(weatherService.callForecastApi(memberInfo.getNickname()))
                        .build());
    }
}
