package com.mqv.monitor.service;

import com.mqv.monitor.dto.AccountDTO;

import javax.security.auth.login.AccountNotFoundException;

public interface AccountService {
    AccountDTO getAccount(int accountId) throws AccountNotFoundException;

    AccountDTO getAccountDefer(int accountId) throws AccountNotFoundException;

    AccountDTO createAccount(String username, String password, String firstName, String lastName);

    Boolean isExists(int accountId);
}
