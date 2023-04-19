package com.mqv.monitor.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.lettuce.core.RedisCommandTimeoutException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jRetryConfig {
    @Bean(name = "redisRetry")
    public Retry redisRetry() {
        RetryConfig config = new RetryConfig.Builder<>()
                .retryOnException(t -> t instanceof RedisCommandTimeoutException)
                .maxAttempts(2)
                .build();
        return Retry.of("redis-retry", config);
    }
}
