package com.overcomingroom.bellbell.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 헤더에서 토큰 추출
        String token = extractToken(request);
        tokenHolder.set(token);
        return true;
    }

    public static String getAccessToken() {
        return tokenHolder.get();
    }

    private String extractToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
