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
        when(accountServiceClient.getAccount("ACCTUNIT1")).thenReturn(account("ACCTUNIT1"));

        service.validateAccountId("ACCTUNIT1");
        service.validateAccountId("ACCTUNIT1");

        verify(accountServiceClient, times(1)).getAccount("ACCTUNIT1");
    }

    @Test
    void evictsLeastRecentlyUsedAccountWhenCacheIsFull() {
        properties.setAccountCacheSize(2);
        AccountValidationService service = new AccountValidationService(accountServiceClient, properties);
        when(accountServiceClient.getAccount("ACCTLRU1")).thenReturn(account("ACCTLRU1"));
        when(accountServiceClient.getAccount("ACCTLRU2")).thenReturn(account("ACCTLRU2"));
        when(accountServiceClient.getAccount("ACCTLRU3")).thenReturn(account("ACCTLRU3"));

        service.validateAccountId("ACCTLRU1");
        service.validateAccountId("ACCTLRU2");
        service.validateAccountId("ACCTLRU1");
        service.validateAccountId("ACCTLRU3");
        service.validateAccountId("ACCTLRU2");

        verify(accountServiceClient, times(1)).getAccount("ACCTLRU1");
        verify(accountServiceClient, times(2)).getAccount("ACCTLRU2");
        verify(accountServiceClient, times(1)).getAccount("ACCTLRU3");
    }

    @Test
    void convertsAccountNotFoundToValidationException() {
        properties.setAccountCacheSize(10);
        AccountValidationService service = new AccountValidationService(accountServiceClient, properties);
        when(accountServiceClient.getAccount("UNKNOWN1")).thenThrow(HttpClientErrorException.NotFound.class);

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
