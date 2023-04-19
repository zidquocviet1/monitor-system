package com.mqv.monitor;

import com.mqv.monitor.redis.FaultToleranceRedisClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev")
public class FaultToleranceRedisClientTest {
    private FaultToleranceRedisClient faultToleranceRedisClient;
    private RedisCommands<String, String> redisCommands;

    @BeforeEach
    void setUp() {
        final var redisClient = mock(RedisClient.class);
        final var stringConnection = mock(StatefulRedisConnection.class);

        final var breakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(1)
                .failureRateThreshold(100f)
                .permittedNumberOfCallsInHalfOpenState(1)
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(Integer.MAX_VALUE))
                .build();

        final var retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ZERO)
                .retryOnException(t -> t instanceof RedisCommandTimeoutException)
                .build();

        this.redisCommands = mock(RedisCommands.class);

        when(redisClient.connect()).thenReturn(stringConnection);
        when(stringConnection.sync()).thenReturn(redisCommands);

        this.faultToleranceRedisClient = new FaultToleranceRedisClient("test", redisClient,
                CircuitBreaker.of("test-breaker", breakerConfig),
                Retry.of("test-retry", retryConfig)
        );
    }

    @Test
    public void testBreaker() {
        when(redisCommands.get(anyString()))
                .thenReturn("ok")
                .thenThrow(new RuntimeException());

        assertEquals("ok", faultToleranceRedisClient.withRedis(connection -> connection.sync().get("key")));

        assertThrows(RedisException.class, () -> faultToleranceRedisClient.withRedis(connection -> connection.sync().get("error")));

        var redisException = assertThrows(RedisException.class, () -> faultToleranceRedisClient.withRedis(connection -> connection.sync().get("cause")));

        assertTrue(redisException.getCause() instanceof CallNotPermittedException);
    }

    @Test
    public void testRetry() {
        when(redisCommands.get(anyString()))
                .thenThrow(new RedisCommandTimeoutException())
                .thenThrow(new RedisCommandTimeoutException())
                .thenReturn("ok");

        assertEquals("ok", faultToleranceRedisClient.withRedis(connection -> connection.sync().get("key")));

        when(redisCommands.get(anyString()))
                .thenThrow(new RedisCommandTimeoutException())
                .thenThrow(new RedisCommandTimeoutException())
                .thenThrow(new RedisCommandTimeoutException())
                .thenReturn("ok");

        assertThrows(RedisCommandTimeoutException.class, () -> faultToleranceRedisClient.withRedis(connection -> connection.sync().get("key")));
    }
}
