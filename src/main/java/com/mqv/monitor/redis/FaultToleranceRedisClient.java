package com.mqv.monitor.redis;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.function.Consumer;
import java.util.function.Function;

public class FaultToleranceRedisClient {
    private final String name;

    private final RedisClient redisClient;
    private final CircuitBreaker connectionCircuitBreaker;
    private final Retry retryConnection;

    private final StatefulRedisConnection<String, String> stringRedisConnection;

    public FaultToleranceRedisClient(String name, RedisClient redisClient, CircuitBreaker circuitBreaker, Retry retry) {
        this.name = name;
        this.redisClient = redisClient;
        this.connectionCircuitBreaker = circuitBreaker;
        this.retryConnection = retry;

        this.stringRedisConnection = redisClient.connect();
    }

    public String getName() { return name; }

    public void useRedis(Consumer<StatefulRedisConnection<String, String>> consumer) {
        useConnection(stringRedisConnection, consumer);
    }

    public <T> T withRedis(Function<StatefulRedisConnection<String, String>, T> function) {
        return withConnection(stringRedisConnection, function);
    }

    private <K, V, T> T withConnection(StatefulRedisConnection<K, V> connection, Function<StatefulRedisConnection<K, V>, T> function) {
        try {
            return connectionCircuitBreaker.executeCheckedSupplier(() -> retryConnection.executeSupplier(() -> function.apply(connection)));
        } catch (Throwable e) {
            if (e instanceof RedisException redisException) {
                throw redisException;
            } else {
                throw new RedisException(e);
            }
        }
    }

    private <K, V> void useConnection(StatefulRedisConnection<K, V> connection, Consumer<StatefulRedisConnection<K, V>> consumer) {
        try {
            connectionCircuitBreaker.executeCheckedRunnable(() -> retryConnection.executeRunnable(() -> consumer.accept(connection)));
        } catch (Throwable e) {
            if (e instanceof RedisException redisException) {
                throw redisException;
            } else {
                throw new RedisException(e);
            }
        }
    }

    void shutdown() {
        stringRedisConnection.close();
        redisClient.shutdown();
    }
}
