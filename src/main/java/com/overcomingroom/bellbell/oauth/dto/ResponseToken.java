package com.overcomingroom.bellbell.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseToken {

  private String accessToken; // 카카오로 부터 응답 받은 AccessToken

}
