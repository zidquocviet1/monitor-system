package com.mqv.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqv.monitor.entity.AccountEntity;
import com.mqv.monitor.ratelimit.RateLimiter;
import com.mqv.monitor.ratelimit.RateLimiterProvider;
import com.mqv.monitor.ratelimit.Resilience4jRateLimiterProvider;
import com.mqv.monitor.redis.FaultToleranceRedisClient;
import com.mqv.monitor.redis.cache.AccountRedisCacheManager;
import com.mqv.monitor.repository.AccountRepository;
import com.mqv.monitor.service.impl.AccountServiceImpl;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RateLimiterProvider rateLimiterProvider;

    @Mock
    private Resilience4jRateLimiterProvider resilience4jRateLimiterProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testGetCachedAccount() throws AccountNotFoundException, JsonProcessingException {
        var accountId = 1;
        var accountEntity = new AccountEntity(accountId, "viet", "mai");
        var checkAccountExistsRateLimiter = mock(RateLimiter.class);
        var faultToleranceRedisClient = mock(FaultToleranceRedisClient.class);
        var stringConnection = mock(StatefulRedisConnection.class);
        var syncCommand = mock(RedisCommands.class);
        var accountJson = """
                {
                    "id": 2,
                    "firstName": "viet",
                    "lastName": "mai"
                }
                """.trim();

        when(faultToleranceRedisClient.withRedis(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Function.class).apply(stringConnection));

        when(stringConnection.sync()).thenReturn(syncCommand);
        when(syncCommand.get("account::" + accountId)).thenReturn(accountJson);

        when(rateLimiterProvider.getCheckAccountExistsRateLimit()).thenReturn(checkAccountExistsRateLimiter);
        when(objectMapper.readValue(accountJson, AccountEntity.class)).thenReturn(accountEntity);

        var accountRedisCacheManager = new AccountRedisCacheManager(faultToleranceRedisClient, objectMapper);
        var accountService = new AccountServiceImpl(accountRepository, rateLimiterProvider,
                resilience4jRateLimiterProvider, accountRedisCacheManager);
        var account = accountService.getAccount(accountId);

        verify(checkAccountExistsRateLimiter).validate(String.valueOf(accountId));

        assertNotNull(account);
        assertEquals(account.accountId(), accountId);
        assertEquals(account.fullName(), "viet mai");
    }

    @Test
    public void giveEmptyRedisCacheData_whenGetAccount_thenCacheIntoRedis() throws JsonProcessingException,
            AccountNotFoundException {
        var accountId = 10;
        var accountEntity = new AccountEntity(accountId, "boot", "spring");
        var checkAccountExistsRateLimiter = mock(RateLimiter.class);
        var faultToleranceRedisClient = mock(FaultToleranceRedisClient.class);
        var stringConnection = mock(StatefulRedisConnection.class);
        var syncCommand = mock(RedisCommands.class);
        var accountJson = """
                {
                    "id": 10,
                    "firstName": "boot",
                    "lastName": "spring"
                }
                """.trim();

        when(stringConnection.sync()).thenReturn(syncCommand);
        when(syncCommand.get(anyString())).thenThrow(new RedisException(""));
        when(objectMapper.writeValueAsString(accountEntity)).thenReturn(accountJson);

        when(rateLimiterProvider.getCheckAccountExistsRateLimit()).thenReturn(checkAccountExistsRateLimiter);
        when(accountRepository.findAccountById(accountId)).thenReturn(Optional.of(accountEntity));

        var accountRedisCacheManager = new AccountRedisCacheManager(faultToleranceRedisClient, objectMapper);
        var accountService = new AccountServiceImpl(accountRepository, rateLimiterProvider,
                resilience4jRateLimiterProvider, accountRedisCacheManager);
        var account = accountService.getAccount(accountId);

        verify(checkAccountExistsRateLimiter).validate(String.valueOf(accountId));

        assertNotNull(account);
        assertEquals(account.accountId(), accountId);
        assertEquals(account.fullName(), "boot spring");
    }
}
