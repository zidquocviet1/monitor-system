package com.mqv.monitor;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.lettuce.core.RedisClient;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableFeignClients
@EnableAspectJAutoProxy // Use this annotation to enable timed aspect
public class MonitorApplication {
    private static final Logger logger = LoggerFactory.getLogger(MonitorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(/*DatadogMeterRegistry datadogMeterRegistry,*/RedisClient redisClient,
                                                                                      RateLimiterRegistry rateLimiterRegistry,
                                                                                      BulkheadRegistry bulkheadRegistry) {
        return args -> {
//            final var defaultDistributionStatisticConfig = new DistributionStatisticConfig.Builder()
//                    .percentiles(.75, .9, .99, .999)
//                    .build();
//            {
//                datadogMeterRegistry.config().commonTags(
//                        Tags.of("host", "127.0.0.1",
//                                "version", "1.0.0-SNAPSHOT",
//                                "env", "dev",
//                                "region", "us-1")
//                ).meterFilter(new MeterFilter() {
//                    @Override
//                    public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
//                        return defaultDistributionStatisticConfig.merge(config);
//                    }
//                });
//            }
//
//            Counter.builder("api.account.exists").register(datadogMeterRegistry);
//            Counter.builder("api.account.not.exists").register(datadogMeterRegistry);
//            Timer.builder("api.get.account").register(datadogMeterRegistry);
//
//            Metrics.addRegistry(datadogMeterRegistry);

//            var pubSubConnection = redisClient.connectPubSub();
//            var command = pubSubConnection.async();
//
//            command.getStatefulConnection().addListener(new RedisPubSubListener<>() {
//                @Override
//                public void message(String channel, String s2) {
//                    logger.info("onMessage({}, {})", channel, s2);
//
//                    Metrics.counter("micrometer.redis." + channel, Tags.of(
//                            Tag.of("redisProtocolVersion", redisClient.getOptions().getProtocolVersion().toString())
//                    )).increment();
//                }
//
//                @Override
//                public void message(String s, String k1, String s2) {
//                    logger.info("onMessage({}, {}, {})", s, k1, s2);
//                }
//
//                @Override
//                public void subscribed(String s, long l) {
//                    logger.info("onSubscribed({}, {})", s, l);
//                }
//
//                @Override
//                public void psubscribed(String s, long l) {
//                    logger.info("onPSubscribed({}, {})", s, l);
//                }
//
//                @Override
//                public void unsubscribed(String s, long l) {
//                    logger.info("onUnsubscribed({}, {})", s, l);
//                }
//
//                @Override
//                public void punsubscribed(String s, long l) {
//                    logger.info("onPUnsubscribed({}, {})", s, l);
//                }
//            });
//            command.subscribe("products", "users");
//
//            redisClient.getResources().eventBus().get().subscribe(event -> logger.info("Redis client resources event: {}", event));
            rateLimiterRegistry.find("checkUserExists").ifPresent(rateLimiter -> rateLimiter.getEventPublisher().onFailure(failureEvent -> {
                logger.debug("Check user exists rate limiter has published a failure event: {}", failureEvent);
                Metrics.counter("resilience4j.ratelimiter." + failureEvent.getRateLimiterName(), "name", failureEvent.getRateLimiterName())
                        .increment();
            }));
            bulkheadRegistry.find("spotify").ifPresent(bulkhead-> bulkhead.getEventPublisher().onCallRejected(rejected -> {
                logger.debug("External call to spotify was rejected: {}", rejected);
                Metrics.counter("resilience4j.bulkhead." + bulkhead.getName(), "name", bulkhead.getName())
                        .increment();
            }));
        };
    }
}