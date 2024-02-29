package com.overcomingroom.bellbell.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Redis 작업을 위한 설정 클래스입니다.
 */
@Configuration
@EnableRedisRepositories
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;
  @Value("${spring.data.redis.port}")
  private int port;

  /**
   * Redis 데이터베이스에 연결하는 RedisConnectionFactory 빈을 정의합니다.
   *
   * @return RedisConnectionFactory
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
  }

  /**
   * Redis 해시 데이터 구조 작업에 사용되는 HashOperations 를 정의하는 빈입니다.
   *
   * @param redisTemplate Redis 작업을 위한 RedisTemplate 빈입니다.
   * @return 해시 데이터 구조 작업을 위한 HashOperations 빈입니다.
   */
  @Bean
  public HashOperations<String, String, String> hashOperations(RedisTemplate<String, String> redisTemplate) {
    return redisTemplate.opsForHash();
  }
}
