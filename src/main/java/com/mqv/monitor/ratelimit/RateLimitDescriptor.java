package com.mqv.monitor.ratelimit;

public interface RateLimitDescriptor {
    String id();

    boolean isDynamic();

    RateLimitConfig config();
}
