package com.mqv.monitor.redis.cache;

import com.mqv.monitor.cache.CacheDescriptor;

import java.time.Duration;

public enum RedisCacheDescriptor implements CacheDescriptor {
    ACCOUNT("account", Duration.ofDays(2).toSeconds()),
    SPOTIFY("spotify", Duration.ofHours(1).toSeconds());

    private final String key;
    private final long timeToLive;

    RedisCacheDescriptor(String key, long timeToLive) {
        this.key = key;
        this.timeToLive = timeToLive;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public long timeToLive() {
        return timeToLive;
    }
}
