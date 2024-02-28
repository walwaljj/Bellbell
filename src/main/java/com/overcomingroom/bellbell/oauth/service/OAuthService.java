package com.overcomingroom.bellbell.oauth.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.dto.KakaoUserInfo;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.oauth.dto.ResponseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

  private final MemberService memberService;

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String clientId;
  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String clientSecret;
  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String redirectUri;
  @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
  private String tokenUrl;


  /**
   * 카카오 OAuth 콜백 처리 메서드입니다.
   *
   * @param code 카카오를 통해 클라이언트 측에서 전달받은 인가 코드
   * @return 카카오로부터 받은 AccessToken
   * <p>
   * TODO: AccessToken 및 RefreshToken Redis 에 저장
   */
  public ResponseToken loginWithKakao(String code) {

    log.info("code: " + code);

    // 액세스 토큰 요청에 필요한 파라미터 설정
    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "authorization_code");
    requestBody.add("client_id", clientId);
    requestBody.add("client_secret", clientSecret);
    requestBody.add("redirect_uri", redirectUri);
    requestBody.add("code", code);

    // RestTemplate을 사용하여 액세스 토큰 요청
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, requestBody,
        String.class);
    if (!tokenResponse.getStatusCode().equals(HttpStatus.OK)) {
      throw new CustomException(ErrorCode.LOGIN_ERROR);
    }
    JSONObject jsonData = new JSONObject(tokenResponse.getBody());
    String accessToken = jsonData.get("access_token").toString();
    String refreshToken = jsonData.get("refresh_token").toString();
    log.info("Access token: " + accessToken);
    log.info("Refresh token: " + refreshToken);

    KakaoUserInfo kakaoUserInfo = memberService.getKakaoUserInfo(new ResponseToken(accessToken));

    // 저장된 사용자가 아니면 저장
    if (memberService.loadMember(kakaoUserInfo).isEmpty()) {
      memberService.saveMember(kakaoUserInfo);
    }

    return new ResponseToken(accessToken);
  }
}
