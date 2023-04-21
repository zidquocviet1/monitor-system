package com.mqv.monitor.ratelimit;

public class RateLimiterProviderMock extends RateLimiterProvider {
    public RateLimiterProviderMock() {
        super(null);
    }

    public RateLimiter getCheckAccountExistsRateLimit() {
        return null;
    }

    public RateLimiter getRegistrationRateLimit() {
        return null;
    }
}
