package com.mqv.monitor.ratelimit;

import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.RedisLuaScript;

public class RateLimiterProvider extends BaseRateLimiter<Descriptor> {
    RateLimiterProvider(RedisLuaScript redisLuaScript) {
        super(redisLuaScript, Descriptor.values());
    }

    public static RateLimiterProvider createProvider(FaultToleranceRedisClient faultToleranceRedisClient) {
        return new RateLimiterProvider(defaultScript(faultToleranceRedisClient));
    }

    public RateLimiter getCheckAccountExistsRateLimit() {
        return forDescriptor(Descriptor.CHECK_ACCOUNT_EXISTS);
    }

    public RateLimiter getRegistrationRateLimit() {
        return forDescriptor(Descriptor.REGISTRATION);
    }
}