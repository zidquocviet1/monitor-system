package com.mqv.monitor.utils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

public record MetricsUtil() {
    private static final String CIRCUIT_BREAKER_CALL_COUNTER_NAME = name(MetricsUtil.class, "breaker", "call");
    private static final String CIRCUIT_BREAKER_STATE_GAUGE_NAME = name(MetricsUtil.class, "breaker", "state");
    private static final String RETRY_CALL_COUNTER_NAME = name(MetricsUtil.class, "retry", "call");

    private static final String NAME_TAG = "name";
    private static final String OUTCOME_TAG = "outcome";

    public static void registerMetrics(CircuitBreaker circuitBreaker, Class<?> clazz) {
        String breakerName = clazz.getName() + "/" + circuitBreaker.getName();

        final Counter successCounter = Metrics.counter(CIRCUIT_BREAKER_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, breakerName),
                Tag.of(OUTCOME_TAG, "success")
        ));

        final Counter failureCounter = Metrics.counter(CIRCUIT_BREAKER_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, breakerName),
                Tag.of(OUTCOME_TAG, "failure")
        ));

        final Counter notPermittedCounter = Metrics.counter(CIRCUIT_BREAKER_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, breakerName),
                Tag.of(OUTCOME_TAG, "notPermitted")
        ));

        circuitBreaker.getEventPublisher().onSuccess(unused -> successCounter.increment());
        circuitBreaker.getEventPublisher().onError(unused -> failureCounter.increment());
        circuitBreaker.getEventPublisher().onCallNotPermitted(unused -> notPermittedCounter.increment());

        Metrics.gauge(CIRCUIT_BREAKER_STATE_GAUGE_NAME,
                Tags.of(Tag.of(NAME_TAG, breakerName)),
                circuitBreaker, breaker -> breaker.getState().getOrder());

    }

    public static void registerMetrics(Retry retry, Class<?> clazz) {
        String retryName = clazz.getName() + "/" + retry.getName();

        final Counter eventRetryCounter = Metrics.counter(RETRY_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, retryName),
                Tag.of(OUTCOME_TAG, "event")
        ));
        final Counter successRetryCounter = Metrics.counter(RETRY_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, retryName),
                Tag.of(OUTCOME_TAG, "success")
        ));
        final Counter errorRetryCounter = Metrics.counter(RETRY_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, retryName),
                Tag.of(OUTCOME_TAG, "error")
        ));
        final Counter retryRetryCounter = Metrics.counter(RETRY_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, retryName),
                Tag.of(OUTCOME_TAG, "retry")
        ));
        final Counter ignoredErrorRetryCounter = Metrics.counter(RETRY_CALL_COUNTER_NAME, Tags.of(
                Tag.of(NAME_TAG, retryName),
                Tag.of(OUTCOME_TAG, "ignoredError")
        ));

        retry.getEventPublisher().onEvent(unused -> eventRetryCounter.increment());
        retry.getEventPublisher().onSuccess(unused -> successRetryCounter.increment());
        retry.getEventPublisher().onError(unused -> errorRetryCounter.increment());
        retry.getEventPublisher().onRetry(unused -> retryRetryCounter.increment());
        retry.getEventPublisher().onIgnoredError(unused -> ignoredErrorRetryCounter.increment());
    }

    public static String name(Class<?> clazz, String... names) {
        String className = clazz.getName();
        StringBuilder nameBuilder = new StringBuilder(className);

        for (String name : names) {
            nameBuilder.append(".");
            nameBuilder.append(name);
        }

        return nameBuilder.toString();
    }
}
