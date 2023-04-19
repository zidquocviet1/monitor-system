package com.mqv.monitor;

import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.RedisLuaScript;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@SpringBootTest
public class RedisLuaScriptTest {

    @Test
    public void testExecute() {
        FaultToleranceRedisClient redisClient = mock(FaultToleranceRedisClient.class);

        StatefulRedisConnection<String, String> stringRedisConnection = mock(StatefulRedisConnection.class);

        RedisCommands<String, String> command = mock(RedisCommands.class);
        RedisAsyncCommands<String, String> asyncCommand = mock(RedisAsyncCommands.class);
        RedisReactiveCommands<String, String> reactiveCommand = mock(RedisReactiveCommands.class);

        when(stringRedisConnection.sync()).thenReturn(command);
        when(stringRedisConnection.async()).thenReturn(asyncCommand);
        when(stringRedisConnection.reactive()).thenReturn(reactiveCommand);

        when(redisClient.withRedis(any(Function.class))).thenAnswer(invocationOnMock ->
                invocationOnMock.getArgument(0, Function.class).apply(stringRedisConnection));

        String script = "return redis.call(\"SET\", KEYS[1], ARGV[1])";
        ScriptOutputType scriptOutputType = ScriptOutputType.VALUE;
        List<String> keys = List.of("create-account");
        List<String> args = List.of("viet.mai:maiquocviet");

        when(stringRedisConnection.sync()
                .eval(script, scriptOutputType, keys.toArray(new String[0]), args.toArray(new String[0])))
                .thenReturn("OK");

        RedisLuaScript redisLuaScript = new RedisLuaScript(redisClient, script, scriptOutputType);
        redisLuaScript.execute(keys, args);

        verify(command).evalsha(redisLuaScript.getSha(), scriptOutputType, keys.toArray(new String[0]), args.toArray(new String[0]));
        verify(command, never()).eval(anyString(), any(), any(), any());
    }
}
