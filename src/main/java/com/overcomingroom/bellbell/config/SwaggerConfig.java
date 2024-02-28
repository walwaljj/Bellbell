package com.overcomingroom.bellbell.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(title = "bellbell",
        description = "bellbell api - overcoming team",
        version = "v1")
)
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi snsOpenApi() {
    String[] paths = {"/v1/**"};

    return GroupedOpenApi.builder()
        .group("bellbell")
        .pathsToMatch(paths)
        .build();
  }
}
