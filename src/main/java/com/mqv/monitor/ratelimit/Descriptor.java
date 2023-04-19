package com.mqv.monitor.ratelimit;

public enum Descriptor implements RateLimitDescriptor {
    CHECK_ACCOUNT_EXISTS("checkAccountExists", false, new RateLimitConfig(30, 10)),

    REGISTRATION("registration", false, new RateLimitConfig(60, 6)),

    PUSH_CHALLENGE("push_challenge", true, new RateLimitConfig(30, 6))
    ;

    private final String id;
    private final boolean isDynamic;
    private final RateLimitConfig config;

    Descriptor(String id, boolean isDynamic, RateLimitConfig config) {
        this.id = id;
        this.isDynamic = isDynamic;
        this.config = config;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isDynamic() {
        return isDynamic;
    }

    @Override
    public RateLimitConfig config() {
        return config;
    }
}
