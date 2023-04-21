package com.mqv.monitor.config;

import com.mqv.monitor.ratelimit.RateLimiterProvider;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.utils.MetricsUtil;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RateLimitConfig {
    @Bean(value = "redisRateLimiterClient")
    public FaultToleranceRedisClient faultToleranceRedisRateLimiterClient(RedisClient redisClient, CircuitBreaker circuitBreaker, @Qualifier("redisRetry") Retry retry) {
        MetricsUtil.registerMetrics(circuitBreaker, FaultToleranceRedisClient.class);
        MetricsUtil.registerMetrics(retry, FaultToleranceRedisClient.class);
        return new FaultToleranceRedisClient("rate_limiters", redisClient, circuitBreaker, retry);
    }

    @Bean(value = "redisCacheClient")
    public FaultToleranceRedisClient faultToleranceRedisCacheClient(RedisClient redisClient, CircuitBreaker circuitBreaker, @Qualifier("redisRetry") Retry retry) {
        MetricsUtil.registerMetrics(circuitBreaker, FaultToleranceRedisClient.class);
        MetricsUtil.registerMetrics(retry, FaultToleranceRedisClient.class);
        return new FaultToleranceRedisClient("cache", redisClient, circuitBreaker, retry);
    }

    @Bean
    public RateLimiterProvider rateLimiterProvider(@Qualifier("redisRateLimiterClient") FaultToleranceRedisClient faultToleranceRedisClient) {
        return RateLimiterProvider.createProvider(faultToleranceRedisClient);
    }
}
