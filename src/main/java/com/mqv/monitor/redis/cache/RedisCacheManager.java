package com.mqv.monitor.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.cache.CacheManager;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface RedisCacheManager extends CacheManager<String, String> {
    <T> Optional<T> get(String key, BiFunction<String, ObjectMapper, T> biFunction);

    void set(String key, Function<ObjectMapper, String> function);

    default void set(String key, Object object) {
        set(key, objectMapper -> {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                return null;
            }
        });
    }

    default <T> Optional<T> get(String key, Class<T> clazz) {
        return get(key, (data, objectMapper) -> {
            try {
                return objectMapper.readValue(data, clazz);
            } catch (JsonProcessingException e) {
                return null;
            }
        });
    }
}
