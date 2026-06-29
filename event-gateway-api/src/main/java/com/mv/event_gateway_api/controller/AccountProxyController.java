package com.mv.event_gateway_api.controller;

import com.mv.event_gateway_api.client.AccountServiceClient;
import com.mv.event_ledger_domain.model.Account;
import com.mv.event_ledger_domain.model.AccountBalance;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountProxyController {

    private final AccountServiceClient accountServiceClient;

    public AccountProxyController(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @Operation(summary = "Proxy account balance from Account Service")
    @GetMapping("/{accountId}/balance")
    public AccountBalance getBalance(@PathVariable String accountId) {
        return accountServiceClient.getBalance(accountId);
    }

    @Operation(summary = "Proxy account details from Account Service")
    @GetMapping("/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        return accountServiceClient.getAccount(accountId);
    }
}
