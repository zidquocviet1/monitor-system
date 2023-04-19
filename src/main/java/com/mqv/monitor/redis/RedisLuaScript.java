package com.mqv.monitor.redis;

import io.lettuce.core.RedisNoScriptException;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RedisLuaScript {
    private static final Logger logger = LoggerFactory.getLogger(RedisLuaScript.class);

    private final FaultToleranceRedisClient redisClient;
    private final ScriptOutputType outputType;
    private final String script;
    private final String sha;

    public RedisLuaScript(FaultToleranceRedisClient redisClient, String script, ScriptOutputType outputType) {
        this.redisClient = redisClient;
        this.script = script;
        this.outputType = outputType;

        try {
            this.sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA1").digest(script.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static RedisLuaScript fromResource(FaultToleranceRedisClient faultToleranceRedisClient, String scriptFile, ScriptOutputType outputType) throws IOException {
        try (InputStream stream = RedisLuaScript.class.getClassLoader().getResourceAsStream(scriptFile)) {
            if (stream == null) {
                throw new IllegalArgumentException("Can't read input stream resource from file: " + scriptFile);
            }
            return new RedisLuaScript(faultToleranceRedisClient,
                    new String(stream.readAllBytes()), outputType);
        }
    }

    public String getSha() {
        return sha;
    }

    public Object execute(List<String> keys, List<String> args) {
        return redisClient.withRedis(connection ->
                execute(connection, keys.toArray(new String[0]), args.toArray(new String[0])));
    }

    public CompletableFuture<Object> executeAsync(List<String> keys, List<String> args) {
        return redisClient.withRedis(connection ->
                executeAsync(connection, keys.toArray(new String[0]), args.toArray(new String[0])));
    }

    public Flux<Object> executeReactive(List<String> keys, List<String> args) {
        return redisClient.withRedis(connection ->
                executeReactive(connection, keys.toArray(new String[0]), args.toArray(new String[0])));
    }

    private <T> Object execute(StatefulRedisConnection<T, T> connection, T[] keys, T[] args) {
        try {
            try {
                return connection.sync().evalsha(sha, outputType, keys, args);
            } catch (RedisNoScriptException ignore) {
                return connection.sync().eval(script, outputType, keys, args);
            }
        } catch (Exception e) {
            logger.error("Failed to execute redis command");
            throw e;
        }
    }

    private <T> CompletableFuture<Object> executeAsync(StatefulRedisConnection<T, T> connection, T[] keys, T[] args) {
        try {
            return connection.async().evalsha(sha, outputType, keys, args).toCompletableFuture();
        } catch (RedisNoScriptException ignore) {
            return connection.async().eval(script, outputType, keys, args).toCompletableFuture();
        }
    }

    private <T> Flux<Object> executeReactive(StatefulRedisConnection<T, T> connection, T[] keys, T[] args) {
        try {
            return connection.reactive().evalsha(sha, outputType, keys, args);
        } catch (RedisNoScriptException ignore) {
            return connection.reactive().eval(script, outputType, keys, args);
        }
    }
}
