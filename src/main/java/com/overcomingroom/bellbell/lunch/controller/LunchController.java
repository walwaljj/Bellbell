package com.overcomingroom.bellbell.lunch.controller;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.interceptor.AuthorizationInterceptor;
import com.overcomingroom.bellbell.lunch.domain.dto.LunchRequestDto;
import com.overcomingroom.bellbell.lunch.service.LunchService;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/lunch")
public class LunchController {

    private final LunchService lunchService;

    @PostMapping
    public ResponseEntity<ResResult> activateLunch(@RequestBody LunchRequestDto lunchRequestDto) {

        String accessToken = AuthorizationInterceptor.getAccessToken();

        // 토큰이 없는 경우 예외 처리
        if (accessToken == null) {
            throw new CustomException(ErrorCode.JWT_VALUE_IS_EMPTY);
        }

        lunchService.activeLunch(accessToken.substring(7), lunchRequestDto);

        ResponseCode responseCode = ResponseCode.LUNCH_ACTIVATE_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build());
    }
}
