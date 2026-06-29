package com.mv.event_gateway_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.mv.event_gateway_api", "com.mv.event_ledger_domain"})
@EntityScan("com.mv.event_ledger_domain.entity")
@EnableJpaRepositories("com.mv.event_ledger_domain.repository")
public class EventGatewayApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventGatewayApiApplication.class, args);
	}

}
