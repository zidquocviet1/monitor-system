package com.mqv.monitor.repository;

import com.mqv.monitor.entity.AccountEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {
    private static final List<AccountEntity> ACCOUNTS = List.of(
            new AccountEntity(1, "Viet", "Mai"),
            new AccountEntity(2, "Viet", "Quoc"),
            new AccountEntity(3, "Mai", "Viet"),
            new AccountEntity(4, "Quoc", "Viet"),
            new AccountEntity(5, "Quoc", "Mai")
    );

    public Optional<AccountEntity> findAccountById(int accountId) {
        // Simulate database call
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ACCOUNTS.stream().filter(account -> account.id() == accountId).findFirst();
    }

    public AccountEntity insertAccount(String username, String password, String firstName, String lastName) {
        var accountId = username.hashCode() + password.hashCode();
        var existing = findAccountById(accountId);

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Account already existed");
        }

        var entity = new AccountEntity(accountId, firstName, lastName);
        // ACCOUNTS.add(entity); // Runtime error
        return entity;
    }
}
