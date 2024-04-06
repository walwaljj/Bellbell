package com.overcomingroom.bellbell.kakaoMessage.service;

import com.overcomingroom.bellbell.kakaoMessage.domain.KakaoMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMessageService {
    private final KaKaoMessageService kaKaoMessageService;
    public boolean sendMessage(String accessToken, String content) {

        KakaoMessageDto kakaoMessageDto = KakaoMessageDto.builder()
                .btnTitle("모든 알림 목록 보기")
                .objType("text")
                .webUrl("http://localhost:8081/")
                .text(content)
                .build();

        return kaKaoMessageService.sendMessage(accessToken, kakaoMessageDto);

    }
}
