package com.bondworks.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuoteBoardCacheService {
  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public QuoteBoardCacheService(StringRedisTemplate redis, ObjectMapper objectMapper) {
    this.redis = redis;
    this.objectMapper = objectMapper;
  }

  public void cacheRfq(UUID rfqId, String status, int timeInForceSeconds) {
    safe(() -> {
      String key = rfqKey(rfqId);
      redis.opsForHash().put(key, "status", status);
      redis.expire(key, Duration.ofSeconds(timeInForceSeconds + 120L));
    });
  }

  public void cacheQuote(UUID rfqId, UUID quoteId, Map<String, Object> quote, int ttlSeconds) {
    safe(() -> {
      String key = rfqKey(rfqId);
      redis.opsForHash().put(key, "quote:" + quoteId, objectMapper.writeValueAsString(quote));
      redis.expire(key, Duration.ofSeconds(ttlSeconds + 120L));
    });
  }

  public void cacheIdempotentResponse(UUID userId, String endpoint, String key, Map<String, Object> response) {
    safe(() -> redis.opsForValue().set(
        "idempotency:" + userId + ":" + endpoint + ":" + key,
        objectMapper.writeValueAsString(response),
        Duration.ofHours(24)));
  }

  private String rfqKey(UUID rfqId) {
    return "rfq-board:" + rfqId;
  }

  private void safe(ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Exception ignored) {
      // Redis is an acceleration layer; PostgreSQL remains the source of truth.
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
