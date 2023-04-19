package com.mqv.monitor.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Resilience4jRateLimiterProvider {
    private final RateLimiterRegistry rateLimiterRegistry;

    public Resilience4jRateLimiterProvider(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    public RateLimiter getCheckUserExistsRateLimiter() throws ClassNotFoundException {
        return rateLimiterRegistry.find("checkUserExists").orElseThrow(ClassNotFoundException::new);
    }
}
