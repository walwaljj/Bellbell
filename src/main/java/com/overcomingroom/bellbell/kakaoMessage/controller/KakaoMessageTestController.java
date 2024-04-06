package com.overcomingroom.bellbell.kakaoMessage.controller;

import com.overcomingroom.bellbell.kakaoMessage.service.CustomMessageService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoMessageTestController {

    private final CustomMessageService messageService;

    @GetMapping("/kakao")
    public String sendMessage(
            @Parameter(hidden = true) @RequestHeader String token, @RequestParam String content
    ){
        messageService.sendMessage(token,content);
        return "전송 완료";
    }
}
