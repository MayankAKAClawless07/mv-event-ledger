package com.mv.account_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.model.Account;
import com.mv.event_ledger_domain.model.AccountTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void appliesTransactionsIdempotentlyAndComputesBalance() throws Exception {
        Account account = Account.builder()
                .accountId("ACCTBAL1")
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();
        AccountTransaction credit = transaction("TXN12345", "ACCTBAL1", Type.CREDIT, "150.00");
        AccountTransaction debit = transaction("TXN67890", "ACCTBAL1", Type.DEBIT, "25.00");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/accounts/ACCTBAL1/transactions")
                        .header("X-Trace-Id", "trace-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credit)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Trace-Id", "trace-account"));

        mockMvc.perform(post("/accounts/ACCTBAL1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credit)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/accounts/ACCTBAL1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debit)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/ACCTBAL1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(125.00));
    }

    @Test
    void createsAccountAndReturnsDetails() throws Exception {
        Account account = Account.builder()
                .accountId("CREATE01")
                .balance(new BigDecimal("25.00"))
                .currency("USD")
                .build();

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("CREATE01"))
                .andExpect(jsonPath("$.balance").value(25.00))
                .andExpect(jsonPath("$.currency").value("USD"));

        mockMvc.perform(get("/accounts/CREATE01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("CREATE01"))
                .andExpect(jsonPath("$.recentTransactions").isArray());
    }

    @Test
    void rejectsInvalidAccountCreateRequest() throws Exception {
        Account account = Account.builder()
                .accountId("bad-id")
                .currency("usd")
                .build();

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void returnsNotFoundForMissingAccount() throws Exception {
        mockMvc.perform(get("/accounts/MISSING1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: MISSING1"));
    }

    @Test
    void rejectsInvalidTransactionRequest() throws Exception {
        AccountTransaction transaction = transaction("BADTXN01", "BADACCT1", Type.CREDIT, "0.00");

        mockMvc.perform(post("/accounts/BADACCT1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("amount: amount must be greater than 0"));
    }

    @Test
    void rejectsTransactionForMissingAccount() throws Exception {
        AccountTransaction transaction = transaction("MISSACC1", "MISSACC1", Type.CREDIT, "10.00");

        mockMvc.perform(post("/accounts/MISSACC1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: MISSACC1"));
    }

    private AccountTransaction transaction(String eventId, String accountId, Type type, String amount) {
        return AccountTransaction.builder()
                .eventId(eventId)
                .accountId(accountId)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency("USD")
                .eventTimestamp(Instant.parse("2026-05-15T14:02:11Z"))
                .build();
    }
}
