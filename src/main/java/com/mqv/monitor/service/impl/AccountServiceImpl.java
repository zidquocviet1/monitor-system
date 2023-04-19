package com.mqv.monitor.service.impl;

import com.mqv.monitor.dto.AccountDTO;
import com.mqv.monitor.entity.AccountEntity;
import com.mqv.monitor.ratelimit.RateLimiterProvider;
import com.mqv.monitor.ratelimit.Resilience4jRateLimiterProvider;
import com.mqv.monitor.redis.cache.AccountRedisCacheManager;
import com.mqv.monitor.repository.AccountRepository;
import com.mqv.monitor.service.AccountService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    private static final String CREATE_ACCOUNT_COUNTER_NAME = "account.service.create.account";
    private static final String ACCOUNT_INFO_COUNTER_NAME = "account.info";
    private static final String ACCOUNT_INFO_TIMER_NAME = "account.info.timer";

    private static final String REGION_TAG_NAME = "region";
    private static final String COUNTRY_CODE_TAG_NAME = "countryCode";
    private static final String USER_AGENT_TAG_NAME = "userAgent";
    private static final String ACCOUNT_ID_TAG_NAME = "accountId";

    private final AccountRepository accountRepository;
    private final RateLimiterProvider rateLimiterProvider;
    private final Resilience4jRateLimiterProvider resilience4jRateLimiterProvider;
    private final AccountRedisCacheManager accountRedisCacheManager;

    public AccountServiceImpl(AccountRepository accountRepository, RateLimiterProvider rateLimiterProvider,
                              Resilience4jRateLimiterProvider resilience4jRateLimiterProvider, AccountRedisCacheManager accountRedisCacheManager) {
        this.accountRepository = accountRepository;
        this.rateLimiterProvider = rateLimiterProvider;
        this.resilience4jRateLimiterProvider = resilience4jRateLimiterProvider;
        this.accountRedisCacheManager = accountRedisCacheManager;
    }

    @Override
    public AccountDTO getAccount(int accountId) throws AccountNotFoundException {
        Metrics.counter(ACCOUNT_INFO_COUNTER_NAME, Tags.of(
                        Tag.of(ACCOUNT_ID_TAG_NAME, String.valueOf(accountId))))
                .increment();

        rateLimiterProvider.getCheckAccountExistsRateLimit().validate(String.valueOf(accountId));

        Optional<AccountEntity> maybeAccount = accountRedisCacheManager.get(accountId);
        if (maybeAccount.isEmpty()) {
            maybeAccount = accountRepository.findAccountById(accountId);
            maybeAccount.ifPresent(entity -> accountRedisCacheManager.set(accountId, entity));
        }
        return maybeAccount.map(account -> new AccountDTO(account.id(), account.firstName() + " " + account.lastName()))
                .orElseThrow(AccountNotFoundException::new);
    }

    @Timed(value = ACCOUNT_INFO_TIMER_NAME, percentiles = {0.75, 0.9, 0.99, 0.999})
    @Override
    public AccountDTO getAccountDefer(int accountId) throws AccountNotFoundException {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return getAccount(accountId);
    }

    @Override
    public AccountDTO createAccount(String username, String password, String firstName, String lastName) {
        try {
            rateLimiterProvider.getRegistrationRateLimit().validate(username);

            var createdAccount = accountRepository.insertAccount(username, password, firstName, lastName);

            Metrics.counter(CREATE_ACCOUNT_COUNTER_NAME, Tags.of(
                            Tag.of(REGION_TAG_NAME, "Asia"),
                            Tag.of(COUNTRY_CODE_TAG_NAME, "VN"),
                            Tag.of(USER_AGENT_TAG_NAME, "Android")))
                    .increment();

            return new AccountDTO(createdAccount.id(), String.join(" ",
                    createdAccount.firstName(), createdAccount.lastName()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Boolean isExists(int accountId) {
        try {
            RateLimiter checkUserExistsRateLimiter = resilience4jRateLimiterProvider.getCheckUserExistsRateLimiter();
            return checkUserExistsRateLimiter.executeSupplier(() -> accountRepository.findAccountById(accountId).isPresent());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
