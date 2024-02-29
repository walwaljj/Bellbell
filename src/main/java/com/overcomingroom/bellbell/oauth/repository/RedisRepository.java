package com.overcomingroom.bellbell.oauth.repository;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

/**
 * Refresh Token 을 저장하는 Redis DAO
 */
@Component
@RequiredArgsConstructor
public class RedisRepository {

  private final HashOperations<String, String, String> hashOperations;

  public void saveToken(String memberId, String accessToken, String refreshToken) {
    // 멤버 ID를 키로 하는 해시 데이터 구조를 가져옵니다.
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("accessToken", accessToken);
    tokenMap.put("refreshToken", refreshToken);
    // Redis 에 맵을 저장합니다.
    hashOperations.putAll("tokens:" + memberId, tokenMap);
  }

  public String getAccessToken(String memberId) {
    // 멤버 ID를 키로 하여 Redis 의 해시 데이터 구조에서 액세스 토큰을 가져옵니다.
    return hashOperations.get("tokens:" + memberId, "accessToken");
  }

  public String getRefreshToken(String memberId) {
    // 멤버 ID를 키로 하여 Redis 의 해시 데이터 구조에서 리프레시 토큰을 가져옵니다.
    return hashOperations.get("tokens:" + memberId, "refreshToken");
  }

}
