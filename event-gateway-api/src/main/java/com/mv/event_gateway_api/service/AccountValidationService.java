package com.mv.event_gateway_api.service;

import com.mv.event_gateway_api.client.AccountServiceClient;
import com.mv.event_gateway_api.config.GatewayProperties;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class AccountValidationService {

    private final AccountServiceClient accountServiceClient;
    private final Map<String, Boolean> accountCache;

    public AccountValidationService(AccountServiceClient accountServiceClient, GatewayProperties properties) {
        this.accountServiceClient = accountServiceClient;
        int maxEntries = Math.max(1, properties.getAccountCacheSize());
        this.accountCache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > maxEntries;
            }
        });
    }

    public void validateAccountId(String accountId) {
        if (Boolean.TRUE.equals(accountCache.get(accountId))) {
            return;
        }
        try {
            accountServiceClient.getAccount(accountId);
            accountCache.put(accountId, Boolean.TRUE);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InvalidAccountException("Invalid accountId: " + accountId);
        }
    }
}
