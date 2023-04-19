package com.mqv.monitor.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jCircuitBreakerConfig {
    @Value("${monitor.resilience4j.circuit-breaker.failure-rate-threshold}")
    private int failureRateThreshold;

    @Value("${monitor.resilience4j.circuit-breaker.sliding-window-size}")
    private int slidingWindowSize;

    @Value("${monitor.resilience4j.circuit-breaker.permitted-number-of-calls-in-half-open-state}")
    private int permittedNumberOfCallsInHalfOpenState;

    @Value("${monitor.resilience4j.circuit-breaker.automatic-transition-from-open-to-half-open-enabled}")
    private boolean automaticTransitionFromOpenToHalfOpenEnabled;

    @Value("${monitor.resilience4j.circuit-breaker.wait-duration-in-open-state}")
    private Duration waitDurationInOpenState;

    @Value("${monitor.resilience4j.circuit-breaker.sliding-window-type}")
    private String slidingWindowType;

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig config = new CircuitBreakerConfig.Builder()
                .failureRateThreshold(failureRateThreshold)
                .slidingWindowSize(slidingWindowSize)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(slidingWindowType))
                .automaticTransitionFromOpenToHalfOpenEnabled(automaticTransitionFromOpenToHalfOpenEnabled)
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .waitDurationInOpenState(waitDurationInOpenState)
                .build();
        return CircuitBreaker.of("redis-breaker", config);
    }
}
