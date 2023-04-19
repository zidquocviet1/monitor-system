package com.mqv.monitor.exception;

import java.time.Duration;

public class RateLimitExceedException extends RuntimeException {
    private final Duration retryAfter;

    public RateLimitExceedException(Duration retryAfter) {
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
