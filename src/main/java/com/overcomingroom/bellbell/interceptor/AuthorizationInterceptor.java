package com.overcomingroom.bellbell.interceptor;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.resolver.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final LoginUserContext loginUserContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 헤더에서 토큰 추출
        String accessToken = extractToken(request);

        log.info("interceptor accessToken = {}", accessToken);
        // 토큰 검증
        if (accessToken == null) {
            throw new CustomException(ErrorCode.JWT_VALUE_IS_EMPTY);
        }

        // 로그인 된 유저 토큰 저장
        registerLoginUser(accessToken);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private String extractToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public void registerLoginUser(String accessToken) {
        loginUserContext.save(accessToken);
    }

    public void releaseLoginUser() {
        loginUserContext.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        releaseLoginUser();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
