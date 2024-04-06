package com.overcomingroom.bellbell.parcel.controller;

import com.overcomingroom.bellbell.parcel.domain.dto.ParcelRequestDto;
import com.overcomingroom.bellbell.parcel.service.ParcelService;
import com.overcomingroom.bellbell.response.ResResult;
import com.overcomingroom.bellbell.response.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/parcel")
public class ParcelController {

  private final ParcelService parcelService;

  /**
   * 택배 알림 생성 요청을 처리합니다.
   *
   * @param accessToken 사용자의 액세스 토큰
   * @param dto         요청 데이터 전송 객체
   * @return ResponseEntity 객체
   */
  @PostMapping("/create")
  public ResponseEntity<ResResult> createParcelNotification(
      @RequestHeader("Authorization") String accessToken,
      final @RequestBody @Valid ParcelRequestDto dto) {
    ResponseCode responseCode = parcelService.createParcelNotification(
        accessToken.substring(7), dto);
    return ResponseEntity.ok(
        ResResult.builder()
            .responseCode(responseCode)
            .code(responseCode.getCode())
            .message(responseCode.getMessage())
            .build()
    );
  }

  /**
   * 택배 알림 목록을 반환합니다.
   *
   * @param accessToken 사용자의 액세스 토큰
   * @return ResponseEntity 객체
   */
  @GetMapping
  public ResponseEntity<ResResult> getParcelNotifications(
      @RequestHeader("Authorization") String accessToken) {
    ResponseCode responseCode = ResponseCode.PARCEL_INFO_GET_SUCCESSFUL;
    return ResponseEntity.ok(
        ResResult.builder()
            .responseCode(responseCode)
            .code(responseCode.getCode())
            .message(responseCode.getMessage())
            .data(parcelService.getParcelNotifications(accessToken.substring(7)))
            .build()
    );
  }

  /**
   * 택배 알림을 삭제합니다.
   *
   * @param accessToken   사용자의 액세스 토큰
   * @param parcelId 삭제할 알림의 ID
   * @return ResponseEntity 객체
   */
  @DeleteMapping("/delete/{parcelId}")
  public ResponseEntity<ResResult> deleteParcelNotification(
      @RequestHeader("Authorization") String accessToken, @PathVariable Long parcelId) {
    ResponseCode responseCode = parcelService.deleteParcelNotification(
        accessToken.substring(7), parcelId);
    return ResponseEntity.ok(
        ResResult.builder()
            .responseCode(responseCode)
            .code(responseCode.getCode())
            .message(responseCode.getMessage())
            .build()
    );
  }
}
