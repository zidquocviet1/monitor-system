package com.mqv.monitor.redis;

import java.util.Optional;

public interface RedisCacheManager<K, T> {
    void delete(K key);

    void set(K key, T value);

    Optional<T> get(K key);
}
