package com.mqv.monitor.config;

import com.mqv.monitor.ratelimit.RateLimiterProvider;
import com.mqv.monitor.ratelimit.RateLimiterProviderMock;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.FaultToleranceRedisClientMock;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class RateLimitConfigMock {
    @Bean(value = "redisRateLimiterClient")
    public FaultToleranceRedisClient faultToleranceRedisRateLimiterClient() {
        var redisClient = mock(RedisClient.class);
        var redisConnection = mock(StatefulRedisConnection.class);

        when(redisClient.connect()).thenReturn(redisConnection);

        return new FaultToleranceRedisClientMock(redisClient);
    }

    @Bean(value = "redisCacheClient")
    public FaultToleranceRedisClient faultToleranceRedisCacheClient() {
        var redisClient = mock(RedisClient.class);
        var redisConnection = mock(StatefulRedisConnection.class);

        when(redisClient.connect()).thenReturn(redisConnection);

        return new FaultToleranceRedisClientMock(redisClient);
    }

    @Bean
    public RateLimiterProvider rateLimiterProvider() {
        return new RateLimiterProviderMock();
    }
}