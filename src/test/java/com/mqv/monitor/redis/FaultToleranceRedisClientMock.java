package com.mqv.monitor.redis;

import io.lettuce.core.RedisClient;

public class FaultToleranceRedisClientMock extends FaultToleranceRedisClient {
    public FaultToleranceRedisClientMock(RedisClient redisClient) {
        super(null, redisClient, null, null);
    }
}
