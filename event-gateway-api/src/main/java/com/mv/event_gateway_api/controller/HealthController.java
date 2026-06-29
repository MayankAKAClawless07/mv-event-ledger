package com.mv.event_gateway_api.controller;

import com.mv.event_gateway_api.client.AccountServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final AccountServiceClient accountServiceClient;

    public HealthController(DataSource dataSource, AccountServiceClient accountServiceClient) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.accountServiceClient = accountServiceClient;
    }

    @Operation(summary = "Gateway health check with dependencies")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        String database = result != null && result == 1 ? "UP" : "DOWN";
        String accountService = accountServiceClient.healthStatus();
        return Map.of(
                "status", "UP".equals(database) && "UP".equals(accountService) ? "UP" : "DEGRADED",
                "service", "event-gateway-api",
                "database", database,
                "dependencies", Map.of("account-service", accountService)
        );
    }
}
