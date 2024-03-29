package com.overcomingroom.bellbell.member.domain.controller;

import com.overcomingroom.bellbell.member.domain.service.MemberService;
import com.overcomingroom.bellbell.resolver.Login;
import com.overcomingroom.bellbell.resolver.LoginUser;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 멤버 관련 API 를 처리하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * 멤버 정보를 반환하는 메서드입니다.
     *
     * @param accessToken 클라이언트를 통해 전달받은 액세스 토큰
     * @return 멤버 정보
     */
    @GetMapping("/v1/member")
    public ResponseEntity<ResResult> getUserInfo(@Parameter(hidden = true) @Login LoginUser loginUser) {

        ResponseCode responseCode = ResponseCode.MEMBER_INFO_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(memberService.getMemberInfo(loginUser.getAccessToken()))
                        .build()
        );
    }
}