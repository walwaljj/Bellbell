package com.overcomingroom.bellbell.kakaoMessage.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KakaoMessageDto {

    private String objType;
    private String text;
    private String webUrl;
    private String mobileUrl;
    private String btnTitle;
}
