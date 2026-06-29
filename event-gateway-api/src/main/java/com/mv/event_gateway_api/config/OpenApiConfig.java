package com.mv.event_gateway_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI gatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Gateway API")
                        .version("1.0.0")
                        .description("Public API for event ingestion, idempotency, and account-service application."));
    }
}
