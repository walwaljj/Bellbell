package com.overcomingroom.bellbell.kakaoMessage.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.kakaoMessage.domain.KakaoMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

@Service
@Slf4j
public class KaKaoMessageService extends HttpCallService {

    @Value("${message.service-uri}")
    private String MSG_SEND_SERVICE_URL;
    private static final String SEND_SUCCESS_MSG = "메시지 전송에 성공했습니다.";
    private static final String SEND_FAIL_MSG = "메시지 전송에 실패했습니다.";

    //kakao api 에서 return 하는 success code 값
    private static final String SUCCESS_CODE = "0";

    public boolean sendMessage(String accessToken, KakaoMessageDto dto) {

        JSONObject templateObject = new JSONObject();
        templateObject.put("object_type", dto.getObjType());
        templateObject.put("text", dto.getText());
        templateObject.put("button_title", dto.getBtnTitle());

        JSONObject linkObject = new JSONObject();
        linkObject.put("web_url", dto.getWebUrl());
        templateObject.put("link", linkObject);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", APP_TYPE_URL_ENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("template_object", templateObject.toString());

        HttpEntity<?> messageRequestEntity = httpClientEntity(httpHeaders, parameters);

        ResponseEntity<String> response = httpRequest(MSG_SEND_SERVICE_URL, HttpMethod.POST, messageRequestEntity);
        JSONObject jsonObject = new JSONObject(response.getBody());
        String resultCode = jsonObject.get("result_code").toString();

        if (!successCheck(resultCode)) {
            throw new CustomException(ErrorCode.MESSAGE_SENDING_FAILED);
        }

        return true;
    }

    private boolean successCheck(String resultCode) {
        if (resultCode.equals(SUCCESS_CODE)) {
            log.info(SEND_SUCCESS_MSG);
            return true;
        } else {
            log.debug(SEND_FAIL_MSG);
            log.info("resultCode = {}", resultCode);
            return false;
        }
    }
}
