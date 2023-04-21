package com.mqv.monitor.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.cache.RedisCacheHandler;
import com.mqv.monitor.redis.cache.RedisCacheManager;
import com.mqv.monitor.utils.Tuple;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class CacheProvider<D extends CacheDescriptor> {
    private final Map<D, RedisCacheManager> mapper;

    public CacheProvider(D[] descriptors, FaultToleranceRedisClient faultToleranceRedisClient, ObjectMapper objectMapper) {
        this.mapper = Arrays.stream(descriptors)
                .map(descriptor -> new Tuple<>(descriptor, createStringRedisCacheManager(descriptor, objectMapper, faultToleranceRedisClient)))
                .collect(Collectors.toMap(Tuple::first, Tuple::second));
    }

    protected RedisCacheManager forDescriptor(D descriptor) {
        return Objects.requireNonNull(mapper.get(descriptor));
    }

    private RedisCacheManager createStringRedisCacheManager(CacheDescriptor descriptor, ObjectMapper objectMapper,
                                                            FaultToleranceRedisClient faultToleranceRedisClient) {
        return new RedisCacheHandler(faultToleranceRedisClient, objectMapper, descriptor.key(), descriptor.timeToLive());
    }
}
