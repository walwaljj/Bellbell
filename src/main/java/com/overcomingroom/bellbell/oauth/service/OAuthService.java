package com.overcomingroom.bellbell.oauth.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OAuthService {

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String clientId;
  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String clientSecret;
  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String redirectUri;
  @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
  private String tokenUrl;
  @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
  private String userInfoUrl;

  /**
   * 카카오 OAuth 콜백 처리 메서드입니다.
   *
   * @param code 카카오에서 전달받은 인가 코드
   * @return 카카오로부터 받은 액세스 토큰 및 관련 정보의 응답
   */
  public ResponseEntity<?> loginWithKakao(String code) {

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
    ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, requestBody, String.class);
    JSONObject jsonData = new JSONObject(tokenResponse.getBody());
    String accessToken = jsonData.get("access_token").toString();

    log.info("Access token: " + accessToken);

    // 받은 액세스 토큰을 사용하여 사용자 정보 요청
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBearerAuth(accessToken);
    RequestEntity<?> requestEntity = RequestEntity.get(userInfoUrl).headers(headers).build();

    // 사용자 정보 요청
    ResponseEntity<String> userInfoResponse = restTemplate.exchange(requestEntity, String.class);
    String userInfo = userInfoResponse.getBody();

    log.info("UserInfo: " + userInfo);
    // 여기서 userInfo를 파싱하여 필요한 작업 수행
    // 예를 들어, 사용자 정보를 데이터베이스에 저장하거나 로그인 처리 등을 수행할 수 있습니다.

    return ResponseEntity.ok(userInfo);
  }
}
