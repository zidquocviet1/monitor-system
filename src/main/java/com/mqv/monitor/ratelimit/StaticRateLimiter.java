package com.mqv.monitor.ratelimit;

import com.mqv.monitor.exception.RateLimitExceedException;
import com.mqv.monitor.redis.RedisLuaScript;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This class will handle three types of rate limit actions:
 * <br></br>
 * 1. Virtual service connection
 * <br></br>
 * 2. Network security
 * <br></br>
 * 3. DNS policy
 */
public class StaticRateLimiter implements RateLimiter {
    private final RedisLuaScript redisLuaScript;

    private final String name;

    private final Counter counter;

    private final RateLimitConfig config;

    public StaticRateLimiter(RedisLuaScript redisLuaScript, String name, RateLimitConfig config) {
        this.redisLuaScript = redisLuaScript;
        this.name = name;
        this.counter = Metrics.counter(StaticRateLimiter.class.getSimpleName() + "." + name, Tags.of(Tag.of("name", name)));
        this.config = config;
    }

    @Override
    public void validate(String key, int amount) {
        boolean isRateLimitExceeded = executeValidateScript(key, amount);
        if (isRateLimitExceeded) {
            counter.increment();
            throw new RateLimitExceedException(Duration.ofSeconds(config.window()));
        }
    }

    private boolean executeValidateScript(String key, int amount) {
        var keys = List.of(rateName(name, key));
        var args = List.of(
                String.valueOf(config.window()),
                String.valueOf(config.size()),
                String.valueOf(amount)
        );
        return (boolean) redisLuaScript.execute(keys, args);
    }

    private CompletionStage<Void> executeValidateScriptAsync(String key, int amount) {
        var keys = List.of(rateName(name, key));
        var args = List.of(
                String.valueOf(config.window()),
                String.valueOf(config.size()),
                String.valueOf(amount)
        );
        return redisLuaScript.executeAsync(keys, args)
                .thenApply(o -> (Boolean) o)
                .thenCompose(isExceed -> {
                    if (isExceed) {
                        counter.increment();
                        return CompletableFuture.failedFuture(new RateLimitExceedException(Duration.ofSeconds(config.window())));
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }

    protected static String rateName(String name, String key) {
        return "sliding_window::" + name + "::" + key;
    }
}
