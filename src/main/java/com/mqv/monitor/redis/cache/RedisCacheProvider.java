package com.mqv.monitor.redis.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.cache.CacheProvider;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheProvider extends CacheProvider<RedisCacheDescriptor> {
    public RedisCacheProvider(@Qualifier("redisCacheClient") FaultToleranceRedisClient faultToleranceRedisClient, ObjectMapper objectMapper) {
        super(RedisCacheDescriptor.values(), faultToleranceRedisClient, objectMapper);
    }

    public RedisCacheManager getAccountCacheManager() {
        return forDescriptor(RedisCacheDescriptor.ACCOUNT);
    }

    public RedisCacheManager getSpotifyCacheManager() {
        return forDescriptor(RedisCacheDescriptor.SPOTIFY);
    }
}
