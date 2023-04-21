package com.mqv.monitor.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.feign.SpotifyAuthClient;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SpotifyRedisCacheManager extends AbstractRedisCacheManager<String, SpotifyAuthClient.AuthResponse> {
    public SpotifyRedisCacheManager(@Qualifier("redisCacheClient") FaultToleranceRedisClient cacheRedisClient, ObjectMapper objectMapper) {
        super(cacheRedisClient, objectMapper, "spotifyRedisCacheManager.redisGet",
                "spotifyRedisCacheManager.redisSet", "spotifyRedisCacheManager.redisDelete");
    }

    @Override
    protected long timeToLive() {
        return Duration.ofHours(1).toSeconds();
    }

    @Override
    protected String generateRedisKey(String data) {
        return "spotify::token::" + data;
    }

    @Override
    protected SpotifyAuthClient.AuthResponse fromJson(ObjectMapper objectMapper, String json) throws JsonProcessingException {
        return objectMapper.readValue(json, SpotifyAuthClient.AuthResponse.class);
    }
}
