package com.mv.event_ledger_domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gateway processing status for a submitted event.")
public enum EventStatus {
    RECEIVED,
    APPLIED,
    FAILED
}
