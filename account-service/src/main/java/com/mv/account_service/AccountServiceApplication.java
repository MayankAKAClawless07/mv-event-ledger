package com.mv.account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.mv.account_service", "com.mv.event_ledger_domain"})
@EntityScan("com.mv.event_ledger_domain.entity")
@EnableJpaRepositories("com.mv.event_ledger_domain.repository")
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
