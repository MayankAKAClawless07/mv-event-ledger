package com.mv.event_ledger_domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction direction. CREDIT increases balance; DEBIT decreases balance.")
public enum Type {
    CREDIT,
    DEBIT
}
