package com.overcomingroom.bellbell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS(Cross-Origin Resource Sharing) 설정을 구성하는 클래스입니다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

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
}
