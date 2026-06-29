package com.mv.account_service.controller;

import com.mv.account_service.service.AccountApplicationService;
import com.mv.event_ledger_domain.model.Account;
import com.mv.event_ledger_domain.model.AccountBalance;
import com.mv.event_ledger_domain.model.AccountTransaction;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountApplicationService accountService;

    public AccountController(AccountApplicationService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create an account")
    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account request) {
        Account response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Apply a transaction to an account")
    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<AccountTransaction> applyTransaction(@PathVariable String accountId,
                                                               @Valid @RequestBody AccountTransaction request) {
        log.info("Applying transaction eventId={} accountId={}", request.getEventId(), accountId);
        AccountTransaction response = accountService.applyTransaction(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get the current account balance")
    @GetMapping("/{accountId}/balance")
    public AccountBalance getBalance(@PathVariable String accountId) {
        return accountService.getBalance(accountId);
    }

    @Operation(summary = "Get account details and recent transactions")
    @GetMapping("/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        return accountService.getAccount(accountId);
    }
}
