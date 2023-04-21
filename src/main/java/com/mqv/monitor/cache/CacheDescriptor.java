package com.mqv.monitor.cache;

public interface CacheDescriptor {
    String key();

    long timeToLive();
}
