package com.overcomingroom.bellbell.oauth.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.dto.KakaoUserInfo;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.oauth.dto.TokenResponse;
import com.overcomingroom.bellbell.oauth.repository.RedisRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

  private final MemberService memberService;
  private final RedisRepository redisRepository;

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
   *
   */
  public String loginWithKakao(String code) {

    // 액세스 토큰 요청에 필요한 파라미터 설정
    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "authorization_code");
    requestBody.add("client_id", clientId);
    requestBody.add("client_secret", clientSecret);
    requestBody.add("redirect_uri", redirectUri);
    requestBody.add("code", code);

    // RestTemplate 을 사용하여 액세스 토큰 요청
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

    KakaoUserInfo kakaoUserInfo = memberService.getKakaoUserInfo(accessToken);

    // 저장된 사용자가 아니면 저장
    Optional<Member> member = memberService.loadMember(kakaoUserInfo);

    Long memberId =
        member.isEmpty() ? memberService.saveMember(kakaoUserInfo).getId() : member.get()
            .getId();

    // Redis 에 토큰 저장
    redisRepository.saveToken(memberId.toString(), accessToken, refreshToken);

    return accessToken;
  }

  // 4주 간격으로 자정에 실행되도록 스케줄링
  @Scheduled(cron = "0 0 0 */4 * *")
  public void refreshTokenTask() {
    // 모든 멤버의 리프레시 토큰을 조회하고 갱신합니다.
    List<Member> members = memberService.getAllMembers();
    for (Member member : members) {
      TokenResponse tokenResponse = getRenewToken(redisRepository.getRefreshToken(member.getId().toString()));
      redisRepository.saveToken(member.getId().toString(), tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }
    log.info("토큰 갱신 완료");

  }

  // 신규 토큰 발급
  public TokenResponse getRenewToken(String refreshToken) {
    // 액세스 토큰 요청에 필요한 파라미터 설정
    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("grant_type", "refresh_token");
    requestBody.add("client_id", clientId);
    requestBody.add("client_secret", clientSecret);
    requestBody.add("refresh_token", refreshToken);

    // RestTemplate 을 사용하여 액세스 토큰 요청
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(tokenUrl, requestBody,
        String.class);
    JSONObject jsonData = new JSONObject(responseEntity.getBody());
    String accessToken = jsonData.get("access_token").toString();

    // 응답받은 데이터 중에 refresh_token 이 존재할 경우 refreshToken 교체
    if(jsonData.has("refresh_token")) {
      refreshToken = jsonData.get("refresh_token").toString();
    }

    return new TokenResponse(accessToken, refreshToken);
  }

}


