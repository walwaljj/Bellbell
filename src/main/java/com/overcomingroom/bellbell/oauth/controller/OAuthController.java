package com.overcomingroom.bellbell.oauth.controller;

import com.overcomingroom.bellbell.oauth.service.OAuthService;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
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
   * @param code 카카오를 통해 클라이언트 측에서 전달받은 인가 코드
   * @return 카카오로부터 받은 AccessToken
   */
  @GetMapping("/v1/login")
  public ResponseEntity<ResResult> loginWithKakao(@RequestParam String code) {
    ResponseCode responseCode = ResponseCode.LOGIN_SUCCESSFUL;
    return ResponseEntity.ok(
        ResResult.builder()
            .responseCode(responseCode)
            .code(responseCode.getCode())
            .message(responseCode.getMessage())
            .data(oAuthService.loginWithKakao(code))
            .build()
    );
  }

}
