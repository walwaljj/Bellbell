package com.overcomingroom.bellbell.member.domain.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.dto.KakaoUserInfo;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.repository.MemberRepository;
import com.overcomingroom.bellbell.oauth.dto.ResponseToken;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
  private String userInfoUrl;

  private final MemberRepository memberRepository;

  /**
   * 카카오에서 받아온 사용자 정보로 회원을 불러옵니다.
   *
   * @param kakaoUserInfo 카카오에서 받아온 사용자 정보
   * @return 불러온 회원 엔티티
   */
  public Optional<Member> loadMember(KakaoUserInfo kakaoUserInfo) {
    return memberRepository.findByEmail(kakaoUserInfo.getEmail());
  }

  /**
   * 카카오에서 받아온 사용자 정보를 회원으로 저장합니다.
   *
   * @param kakaoUserInfo 카카오에서 받아온 사용자 정보
   * @return 저장된 회원 엔티티
   */
  public Member saveMember(KakaoUserInfo kakaoUserInfo) {
    return memberRepository.save(kakaoUserInfo.toEntity());
  }

  /**
   * 액세스 토큰으로 카카오에서 사용자정보를 받아옵니다.
   *
   * @param responseToken 클라이언트로부터 받아온 액세스 토큰
   * @return 카카오 사용자 정보
   */

  public KakaoUserInfo getKakaoUserInfo(ResponseToken responseToken) {
    log.info(responseToken.getAccessToken());
    // 받은 액세스 토큰을 사용하여 사용자 정보 요청
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBearerAuth(responseToken.getAccessToken());
    RequestEntity<?> requestEntity = RequestEntity.get(userInfoUrl).headers(headers).build();

    // 사용자 정보 요청
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> userInfoResponse = restTemplate.exchange(requestEntity, String.class);
    String userInfo = userInfoResponse.getBody();

    log.info("UserInfo: " + userInfo);

    JSONObject userInfoData = new JSONObject(userInfoResponse.getBody());
    String email = userInfoData.getJSONObject("kakao_account").get("email").toString();
    String nickname = userInfoData.getJSONObject("properties").get("nickname").toString();
    KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(nickname, email);

    log.info("email: {}", email);
    log.info("nickname: {}", nickname);

    return kakaoUserInfo;
  }

  /**
   * 액세스 토큰으로 카카오에서 받아온 사용자정보를 통해 DB에 저장된 멤버 정보를 반환합니다.
   *
   * @param accessToken 클라이언트로부터 받아온 액세스 토큰
   * @return Member 정보가 저장된 DTO
   */
  public KakaoUserInfo getMemberInfo(String accessToken) {
    Member member = loadMember(getKakaoUserInfo(new ResponseToken(accessToken))).orElseThrow(
        () -> new CustomException(ErrorCode.MEMBER_INVALID));
    return new KakaoUserInfo(member.getNickname(), member.getEmail());
  }

}
