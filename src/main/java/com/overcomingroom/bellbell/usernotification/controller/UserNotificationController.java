package com.overcomingroom.bellbell.usernotification.controller;

import com.overcomingroom.bellbell.resolver.Login;
import com.overcomingroom.bellbell.resolver.LoginUser;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import com.overcomingroom.bellbell.usernotification.domain.dto.UserNotificationRequestDto;
import com.overcomingroom.bellbell.usernotification.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    /**
     * 사용자 알림 생성 요청을 처리합니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @param dto         요청 데이터 전송 객체
     * @return ResponseEntity 객체
     */
    @PostMapping("/create")
    public ResponseEntity<ResResult> createUserNotification(
            @Parameter(hidden = true) @Login LoginUser loginUser,
            final @RequestBody @Valid UserNotificationRequestDto dto) {

        ResponseCode responseCode = userNotificationService.createUserNotification(
                loginUser.getAccessToken(), dto);

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build()
        );
    }

    /**
     * 사용자의 알림 목록을 반환합니다.
     *
     * @param accessToken 사용자의 액세스 토큰
     * @return ResponseEntity 객체
     */
    @GetMapping
    public ResponseEntity<ResResult> getUserNotifications(@Parameter(hidden = true) @Login LoginUser loginUser) {

        ResponseCode responseCode = ResponseCode.USER_NOTIFICATIONS_GET_SUCCESSFUL;

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .data(userNotificationService.getUserNotifications(loginUser.getAccessToken()))
                        .build()
        );
    }

    /**
     * 사용자의 특정 알림을 삭제합니다.
     *
     * @param accessToken    사용자의 액세스 토큰
     * @param notificationId 삭제할 알림의 ID
     * @return ResponseEntity 객체
     */
    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<ResResult> deleteUserNotification(
            @Parameter(hidden = true) @Login LoginUser loginUser,
            @PathVariable Long notificationId) {

        ResponseCode responseCode = userNotificationService.deleteUserNotification(
                loginUser.getAccessToken(), notificationId);

        return ResponseEntity.ok(
                ResResult.builder()
                        .responseCode(responseCode)
                        .code(responseCode.getCode())
                        .message(responseCode.getMessage())
                        .build()
        );
    }
}
