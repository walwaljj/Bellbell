package com.overcomingroom.bellbell.config;

import com.overcomingroom.bellbell.interceptor.AuthorizationInterceptor;
import com.overcomingroom.bellbell.resolver.LoginUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 설정을 구성하는 클래스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;
    private final LoginUserResolver loginUserResolver;

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 추가합니다.
     *
     * @param registry CORS 설정 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081") // 허용할 Origin 을 지정합니다. 필요에 따라 변경 가능합니다.
                .allowedMethods("*") // 모든 HTTP 메서드를 허용합니다.
                .allowedHeaders("*") // 모든 헤더를 허용합니다.
                .allowCredentials(true) // 인증 정보를 허용합니다.
                .maxAge(3600); // preflight 요청의 캐시 시간을 설정합니다.
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authorizationInterceptor)
                .addPathPatterns("/**") // 모든 요청은 인터셉터를 거치도록 함.
                .excludePathPatterns("/v1/login") // 로그인 시 AccessToken이 없기 때문에 해당 요청만 제외
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"); // 스웨거 예외 처리
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserResolver);
    }
}
