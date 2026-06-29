package com.mv.event_gateway_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "account-service")
public class GatewayProperties {

    private String baseUrl = "http://localhost:8081";
    private int accountCacheSize = 1000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getAccountCacheSize() {
        return accountCacheSize;
    }

    public void setAccountCacheSize(int accountCacheSize) {
        this.accountCacheSize = accountCacheSize;
    }
}
