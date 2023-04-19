package com.mqv.monitor.ratelimit;

import com.mqv.monitor.redis.RedisLuaScript;

/**
 * This class will handle only one type of rate limit action:
 * <br></br>
 * 1. Application profile
 * */
public class DynamicRateLimiter implements RateLimiter {
    private final RedisLuaScript redisLuaScript;

    private final String id;

    private final RateLimitConfig rateLimitConfig;

    public DynamicRateLimiter(RedisLuaScript redisLuaScript, String id, RateLimitConfig rateLimitConfig) {
        this.redisLuaScript = redisLuaScript;
        this.id = id;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void validate(String key, int amount) {
        current().validate(key, amount);
    }

    private RateLimiter current() {
        return new StaticRateLimiter(redisLuaScript, id, rateLimitConfig);
    }
}
