package com.mv.event_gateway_api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class AppConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder, TraceInterceptor traceInterceptor) {
        return builder
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(2))
                .additionalInterceptors(traceInterceptor)
                .build();
    }
}
