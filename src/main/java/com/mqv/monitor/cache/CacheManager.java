package com.mqv.monitor.cache;

import java.util.Optional;

public interface CacheManager<K, T> {
    void delete(K key);

    void set(K key, T value);

    Optional<T> get(K key);
}
