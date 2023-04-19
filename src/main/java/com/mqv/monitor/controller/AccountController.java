package com.mqv.monitor.controller;

import com.mqv.monitor.dto.AccountDTO;
import com.mqv.monitor.request.CreateAccountRequest;
import com.mqv.monitor.service.AccountService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> checkExists(@PathVariable("id") int accountId) {
        return ResponseEntity.ok(accountService.isExists(accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable("id") Integer accountId) {
        try {
            return ResponseEntity.ok(accountService.getAccount(accountId));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/defer/{id}")
    public ResponseEntity<AccountDTO> getAccountDefer(@PathVariable("id") Integer accountId) {
        try {
            return ResponseEntity.ok(accountService.getAccountDefer(accountId));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Timed(value = "create.account")
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request.username(),
                request.password(), request.firstName(), request.lastName()));
    }
}
