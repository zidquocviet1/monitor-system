package com.mqv.monitor;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.connection.ConnectionEvent;
import io.lettuce.core.resource.ClientResources;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(RedisIntegrationTest.class);

    /**
     * We have 3 popular java redis client libraries: Redisson, Jedis, Lettuce
     * */
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> stringRedisConnection;

    @BeforeEach
    void setUp() {
        var clientResource = ClientResources.builder().build();

        this.redisClient = RedisClient.create(clientResource, "redis://localhost:6379");
        this.stringRedisConnection = this.redisClient.connect();

        clientResource.eventBus().get().subscribe(event -> {
            if (event instanceof ConnectionEvent) {
                LOG.info("Connection event: {}", event);
            } else if (event instanceof ClusterTopologyChangedEvent) {
                LOG.info("Cluster changed event: {}", event);
            }
        });
    }

    @AfterEach
    void tearDown() {
        this.stringRedisConnection.close();
        this.redisClient.close();
    }
}
