package com.mv.event_gateway_api.client;

import com.mv.event_gateway_api.config.GatewayProperties;
import com.mv.event_ledger_domain.model.Account;
import com.mv.event_ledger_domain.model.AccountBalance;
import com.mv.event_ledger_domain.model.AccountTransaction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final GatewayProperties properties;

    public AccountServiceClient(RestTemplate restTemplate, GatewayProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    @RateLimiter(name = "accountService")
    public AccountTransaction applyTransaction(String accountId, AccountTransaction transaction) {
        return restTemplate.postForObject(
                properties.getBaseUrl() + "/accounts/{accountId}/transactions",
                transaction,
                AccountTransaction.class,
                accountId
        );
    }

    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    @RateLimiter(name = "accountService")
    public AccountBalance getBalance(String accountId) {
        return restTemplate.getForObject(
                properties.getBaseUrl() + "/accounts/{accountId}/balance",
                AccountBalance.class,
                accountId
        );
    }

    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    @RateLimiter(name = "accountService")
    public boolean accountExists(String accountId) {
        try {
            restTemplate.getForObject(
                    properties.getBaseUrl() + "/accounts/{accountId}",
                    Account.class,
                    accountId
            );
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    @RateLimiter(name = "accountService")
    public Account getAccount(String accountId) {
        return restTemplate.getForObject(
                properties.getBaseUrl() + "/accounts/{accountId}",
                Account.class,
                accountId
        );
    }

    public String healthStatus() {
        try {
            Map<?, ?> response = restTemplate.getForObject(properties.getBaseUrl() + "/health", Map.class);
            Object status = response == null ? null : response.get("status");
            return "UP".equals(status) ? "UP" : "DOWN";
        } catch (RestClientException ex) {
            return "DOWN";
        }
    }
}
