package com.mqv.monitor.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.sync.RedisCommands;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractRedisCacheManager<K, T> implements RedisCacheManager<K, T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRedisCacheManager.class);

    protected final FaultToleranceRedisClient cacheRedisClient;

    protected final ObjectMapper objectMapper;

    private final String getTimerName;
    private final String setTimerName;
    private final String deleteTimerName;

    protected AbstractRedisCacheManager(FaultToleranceRedisClient cacheRedisClient, ObjectMapper objectMapper,
                                        String getTimerName, String setTimerName, String deleteTimerName) {
        this.cacheRedisClient = cacheRedisClient;
        this.objectMapper = objectMapper;
        this.getTimerName = getTimerName;
        this.setTimerName = setTimerName;
        this.deleteTimerName = deleteTimerName;
    }

    protected abstract long timeToLive();

    protected abstract String generateRedisKey(K key);

    protected abstract T fromJson(ObjectMapper objectMapper, String json) throws JsonProcessingException;

    @Override
    public void delete(K key) {
        Metrics.timer(deleteTimerName).record(() -> cacheRedisClient.useRedis(connection -> connection.sync().del(generateRedisKey(key))));
    }

    @Override
    public void set(K key, T value) {
        Metrics.timer(setTimerName).record(() -> {
            try {
                String jsonData = objectMapper.writeValueAsString(value);

                cacheRedisClient.useRedis(connection -> {
                    RedisCommands<String, String> redisAsyncCommands = connection.sync();
                    redisAsyncCommands.setex(generateRedisKey(key), timeToLive(), jsonData);
                });
            } catch (JsonProcessingException e) {
                logger.warn("Can't parse data json to insert into redis");
            }
        });
    }

    @Override
    public Optional<T> get(K key) {
        return Metrics.timer(getTimerName).record(() -> {
            try {
                String jsonData = cacheRedisClient.withRedis(connection -> connection.sync().get(generateRedisKey(key)));
                if (jsonData != null) {
                    try {
                        T data = fromJson(objectMapper, jsonData);
                        return Optional.of(data);
                    } catch (JsonProcessingException e) {
                        logger.warn("Data with key={} was founded from redis cache but can't parse", key);
                    }
                }
                return Optional.empty();
            } catch (RedisException e) {
                logger.warn("Redis failure.", e);
                return Optional.empty();
            }
        });
    }
}
