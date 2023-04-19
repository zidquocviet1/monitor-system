package com.mqv.monitor.ratelimit;

public interface RateLimiter {
    void validate(String key, int amount);

    default void validate(String key) {
        validate(key, 1);
    }
}
