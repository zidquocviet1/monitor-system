package com.mqv.monitor.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.entity.AccountEntity;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AccountRedisCacheManager extends AbstractRedisCacheManager<Integer, AccountEntity> {
    public AccountRedisCacheManager(@Qualifier("redisCacheClient") FaultToleranceRedisClient cacheRedisClient, ObjectMapper objectMapper) {
        super(cacheRedisClient, objectMapper, "accountRedisCacheManager.redisGet",
                "accountRedisCacheManager.redisSet", "accountRedisCacheManager.redisDelete");
    }

    @Override
    protected long timeToLive() {
        return Duration.ofDays(2).toSeconds();
    }

    @Override
    protected String generateRedisKey(Integer key) {
        return "account::" + key;
    }

    @Override
    protected AccountEntity fromJson(ObjectMapper objectMapper, String json) throws JsonProcessingException {
        return objectMapper.readValue(json, AccountEntity.class);
    }
}
