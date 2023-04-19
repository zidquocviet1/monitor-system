package com.mqv.monitor.ratelimit;

import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.RedisLuaScript;
import com.mqv.monitor.utils.Tuple;
import io.lettuce.core.ScriptOutputType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseRateLimiter<T extends RateLimitDescriptor> {
    protected final Map<T, RateLimiter> rateLimitDescriptors;

    BaseRateLimiter(RedisLuaScript redisLuaScript, T[] descriptors) {
        this.rateLimitDescriptors = Arrays.stream(descriptors)
                .map(descriptor -> new Tuple<>(descriptor, createRateLimitByDescriptor(descriptor, redisLuaScript)))
                .collect(Collectors.toUnmodifiableMap(Tuple::first, Tuple::second));
    }

    protected static RedisLuaScript defaultScript(FaultToleranceRedisClient faultToleranceRedisClient) {
        try {
            return RedisLuaScript.fromResource(faultToleranceRedisClient, "lua/sliding_window_rate_limit.lua", ScriptOutputType.BOOLEAN);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load validate limit lua script file");
        }
    }

    protected RateLimiter forDescriptor(T descriptor) {
        return Objects.requireNonNull(rateLimitDescriptors.get(descriptor));
    }

    private RateLimiter createRateLimitByDescriptor(RateLimitDescriptor descriptor, RedisLuaScript redisLuaScript) {
        RateLimiter rateLimiter;
        if (descriptor.isDynamic()) {
            rateLimiter = new DynamicRateLimiter(redisLuaScript, descriptor.id(), descriptor.config());
        } else {
            rateLimiter = new StaticRateLimiter(redisLuaScript, descriptor.id(), descriptor.config());
        }
        return rateLimiter;
    }
}
