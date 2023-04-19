package com.mqv.monitor.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${monitor.redis.url}")
    private String url;

    @Value("${monitor.redis.port}")
    private int port;

    @Bean
    public RedisClient redisClient(ClientResources clientResources) {
        return RedisClient.create(clientResources, new RedisURI(url, port, Duration.ofSeconds(10)));
    }

    @Bean
    public ClientResources clientResources() {
        return ClientResources.builder().build();
    }
}
