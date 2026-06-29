package com.mv.event_gateway_api.service;

import com.mv.event_gateway_api.client.AccountServiceClient;
import com.mv.event_gateway_api.config.GatewayProperties;
import com.mv.event_ledger_domain.model.Account;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountValidationServiceTest {

    private final AccountServiceClient accountServiceClient = mock(AccountServiceClient.class);
    private final GatewayProperties properties = new GatewayProperties();

    @Test
    void cachesSuccessfulAccountValidation() {
        properties.setAccountCacheSize(10);
        AccountValidationService service = new AccountValidationService(accountServiceClient, properties);
        when(accountServiceClient.accountExists("ACCTUNIT1")).thenReturn(true);

        service.validateAccountId("ACCTUNIT1");
        service.validateAccountId("ACCTUNIT1");

        verify(accountServiceClient, times(1)).accountExists("ACCTUNIT1");
    }

    @Test
    void evictsLeastRecentlyUsedAccountWhenCacheIsFull() {
        properties.setAccountCacheSize(2);
        AccountValidationService service = new AccountValidationService(accountServiceClient, properties);
        when(accountServiceClient.accountExists("ACCTLRU1")).thenReturn(true);
        when(accountServiceClient.accountExists("ACCTLRU2")).thenReturn(true);
        when(accountServiceClient.accountExists("ACCTLRU3")).thenReturn(true);

        service.validateAccountId("ACCTLRU1");
        service.validateAccountId("ACCTLRU2");
        service.validateAccountId("ACCTLRU1");
        service.validateAccountId("ACCTLRU3");
        service.validateAccountId("ACCTLRU2");

        verify(accountServiceClient, times(1)).accountExists("ACCTLRU1");
        verify(accountServiceClient, times(2)).accountExists("ACCTLRU2");
        verify(accountServiceClient, times(1)).accountExists("ACCTLRU3");
    }

    @Test
    void convertsAccountNotFoundToValidationException() {
        properties.setAccountCacheSize(10);
        AccountValidationService service = new AccountValidationService(accountServiceClient, properties);
        when(accountServiceClient.accountExists("UNKNOWN1")).thenReturn(false);

        assertThatThrownBy(() -> service.validateAccountId("UNKNOWN1"))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessage("Invalid accountId: UNKNOWN1");
    }

    private Account account(String accountId) {
        return Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();
    }
}
