package com.mv.event_gateway_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.model.Account;
import com.mv.event_ledger_domain.model.Event;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "account-service.base-url=http://account-service.test",
        "resilience4j.circuitbreaker.instances.accountService.slidingWindowSize=100",
        "resilience4j.circuitbreaker.instances.accountService.minimumNumberOfCalls=100",
        "resilience4j.retry.instances.accountService.maxAttempts=1",
        "resilience4j.ratelimiter.instances.accountService.limitForPeriod=100"
})
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
class EventGatewayApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitsEventPropagatesTraceAndDuplicateDoesNotCallAccountServiceAgain() throws Exception {
        Event event = event("ABC12345", "ACCT12345", Type.CREDIT, "2026-05-15T14:02:11Z");
        server.expect(once(), requestTo("http://account-service.test/accounts/ACCT12345"))
                .andExpect(header("X-Trace-Id", "trace-123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(account("ACCT12345")), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://account-service.test/accounts/ACCT12345/transactions"))
                .andExpect(header("X-Trace-Id", "trace-123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/events")
                        .header("X-Trace-Id", "trace-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("ABC12345"))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        mockMvc.perform(post("/events")
                        .header("X-Trace-Id", "trace-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("ABC12345"));

        server.verify();
    }

    @Test
    void listsEventsForAccountOrderedByEventTimestamp() throws Exception {
        Event later = event("LATER123", "ORDER123", Type.CREDIT, "2026-05-15T14:02:11Z");
        Event earlier = event("EARLY123", "ORDER123", Type.DEBIT, "2026-05-14T14:02:11Z");
        server.expect(once(), requestTo("http://account-service.test/accounts/ORDER123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(account("ORDER123")), MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://account-service.test/accounts/ORDER123/transactions"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(later), MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://account-service.test/accounts/ORDER123/transactions"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(earlier), MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(later)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(earlier)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/events").param("account", "ORDER123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventId").value("EARLY123"))
                .andExpect(jsonPath("$[1].eventId").value("LATER123"));
    }

    @Test
    void returnsServiceUnavailableWhenAccountServiceFails() throws Exception {
        Event event = event("FAIL1234", "FAILACCT", Type.CREDIT, "2026-05-15T14:02:11Z");
        server.expect(requestTo("http://account-service.test/accounts/FAILACCT"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(account("FAILACCT")), MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://account-service.test/accounts/FAILACCT/transactions"))
                .andRespond(withServerError());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Account Service unavailable"));
    }

    @Test
    void rejectsUnknownAccountIdBeforeApplyingTransaction() throws Exception {
        Event event = event("MISS1234", "MISSACCT", Type.CREDIT, "2026-05-15T14:02:11Z");
        server.expect(requestTo("http://account-service.test/accounts/MISSACCT"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid accountId: MISSACCT"));
    }

    @Test
    void returnsServiceUnavailableWhenAccountValidationDependencyFails() throws Exception {
        Event event = event("VALFAIL1", "VALFAIL1", Type.CREDIT, "2026-05-15T14:02:11Z");
        server.expect(requestTo("http://account-service.test/accounts/VALFAIL1"))
                .andRespond(withServerError());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Account Service unavailable"));
    }

    @Test
    void rejectsInvalidEventRequestBody() throws Exception {
        Event event = event("BAD12345", "BADACCT1", Type.CREDIT, "2026-05-15T14:02:11Z");
        event.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("amount: amount must be greater than 0"));
    }

    private Event event(String eventId, String accountId, Type type, String timestamp) {
        return Event.builder()
                .eventId(eventId)
                .accountId(accountId)
                .type(type)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .eventTimestamp(Instant.parse(timestamp))
                .build();
    }

    private Account account(String accountId) {
        return Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();
    }
}
