package com.mv.event_ledger_domain.model;

import com.mv.event_ledger_domain.util.RandomIdGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Current account balance response.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

    @Schema(description = "Random alphanumeric account identifier.", example = "9c0f6f1d2e3a4b5c8d7e6f5a4b3c2d1e")
    @Builder.Default
    private String accountId = RandomIdGenerator.randomAccountId();

    @Schema(description = "Net account balance.", example = "125.00")
    private BigDecimal balance;

    @Schema(description = "Currency for this account.", example = "USD")
    private String currency;
}
