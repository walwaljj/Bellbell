package com.overcomingroom.bellbell.oauth.controller;

import com.overcomingroom.bellbell.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카카오 OAuth 인증을 처리하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class OAuthController {

  private final OAuthService oAuthService;

  /**
   * 카카오 OAuth 콜백 처리 메서드입니다.
   *
   * @param code 카카오에서 전달받은 인가 코드
   * @return 카카오로부터 받은 액세스 토큰 및 관련 정보의 응답
   */
  @GetMapping("/login/oauth2/code/kakao")
  public ResponseEntity<?> loginWithKakao(@RequestParam String code) {
    return oAuthService.loginWithKakao(code);
  }
}
