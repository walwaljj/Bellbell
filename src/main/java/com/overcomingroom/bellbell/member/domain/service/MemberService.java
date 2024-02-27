package com.overcomingroom.bellbell.member.domain.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.member.domain.dto.KakaoUserInfo;
import com.overcomingroom.bellbell.member.domain.entity.Member;
import com.overcomingroom.bellbell.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  /**
   * 카카오에서 받아온 사용자 정보로 회원을 불러옵니다.
   * @param kakaoUserInfo 카카오에서 받아온 사용자 정보
   * @return 불러온 회원 엔티티
   */
  public Member loadKakaoUser(KakaoUserInfo kakaoUserInfo) {
    return memberRepository.findByEmail(kakaoUserInfo.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.LOGIN_ERROR));
  }

  /**
   * 카카오에서 받아온 사용자 정보를 회원으로 저장합니다.
   * @param kakaoUserInfo 카카오에서 받아온 사용자 정보
   * @return 저장된 회원 엔티티
   */
  public Member saveKakaoUser(KakaoUserInfo kakaoUserInfo) {
    return memberRepository.save(kakaoUserInfo.toEntity());
  }

}
