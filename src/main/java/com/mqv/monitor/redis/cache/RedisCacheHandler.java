package com.mqv.monitor.redis.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.sync.RedisCommands;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RedisCacheHandler implements RedisCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheHandler.class);

    private final FaultToleranceRedisClient cacheRedisClient;

    private final ObjectMapper objectMapper;

    private final String key;

    private final long timeToLive;

    private final String getTimerName;
    private final String setTimerName;
    private final String deleteTimerName;

    public RedisCacheHandler(FaultToleranceRedisClient cacheRedisClient, ObjectMapper objectMapper, String key, long timeToLive) {
        this.cacheRedisClient = cacheRedisClient;
        this.objectMapper = objectMapper;
        this.key = key;
        this.timeToLive = timeToLive;
        this.getTimerName = "redisCacheHandler." + key + ".redisGet";
        this.setTimerName = "redisCacheHandler." + key + ".redisSet";
        this.deleteTimerName = "redisCacheHandler." + key + ".redisDelete";
    }

    @Override
    public void delete(String key) {
        Metrics.timer(deleteTimerName).record(() -> cacheRedisClient.useRedis(connection -> connection.sync().del(key)));
    }

    @Override
    public void set(String key, String jsonData) {
        Metrics.timer(setTimerName).record(() -> cacheRedisClient.useRedis(connection -> {
            RedisCommands<String, String> redisAsyncCommands = connection.sync();
            redisAsyncCommands.setex(generateRedisKey(key), timeToLive, jsonData);
        }));
    }

    @Override
    public void set(String key, Function<ObjectMapper, String> function) {
        String jsonData = function.apply(objectMapper);
        if (jsonData != null) {
            set(key, jsonData);
        }
    }

    @Override
    public Optional<String> get(String key) {
        return Metrics.timer(getTimerName).record(() -> {
            try {
                String jsonData = cacheRedisClient.withRedis(connection -> connection.sync().get(generateRedisKey(key)));
                if (jsonData != null) {
                    return Optional.of(jsonData);
                }
                return Optional.empty();
            } catch (RedisException e) {
                logger.warn("Redis failure.", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public <T> Optional<T> get(String key, BiFunction<String, ObjectMapper, T> biFunction) {
        return get(key).map(jsonData -> biFunction.apply(jsonData, objectMapper));
    }

    private String generateRedisKey(String key) {
        return this.key + "::" + key;
    }
}
