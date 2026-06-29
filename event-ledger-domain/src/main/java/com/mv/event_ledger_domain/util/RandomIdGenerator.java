package com.mv.event_ledger_domain.util;

import java.security.SecureRandom;

public final class RandomIdGenerator {

    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 32;

    private RandomIdGenerator() {
    }

    public static String randomAlphanumeric() {
        StringBuilder value = new StringBuilder(DEFAULT_LENGTH);
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            value.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return value.toString();
    }

    public static String randomEventId() {
        return randomAlphanumeric();
    }

    public static String randomAccountId() {
        return randomAlphanumeric();
    }
}
