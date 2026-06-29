package com.mv.account_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Operation(summary = "Account Service health check")
    @GetMapping("/health")
    public Map<String, Object> health() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Map.of(
                "status", "UP",
                "service", "account-service",
                "database", result != null && result == 1 ? "UP" : "DOWN"
        );
    }
}
