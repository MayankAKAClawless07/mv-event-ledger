package com.mv.event_ledger_domain.model;

import com.mv.event_ledger_domain.util.RandomIdGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Account details with current balance and recent transactions.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Schema(description = "Random alphanumeric account identifier.", example = "9c0f6f1d2e3a4b5c8d7e6f5a4b3c2d1e")
    @NotBlank(message = "accountId is required")
    @Pattern(regexp = "^[A-Za-z0-9]{8,80}$", message = "accountId must be an alphanumeric string between 8 and 80 characters")
    @Builder.Default
    private String accountId = RandomIdGenerator.randomAccountId();

    @Schema(description = "Current net balance, calculated as credits minus debits.", example = "125.00")
    private BigDecimal balance;

    @Schema(description = "Account currency.", example = "USD")
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter uppercase ISO code")
    private String currency;

    @Schema(description = "Recent account transactions ordered by event timestamp descending.")
    @Builder.Default
    private List<AccountTransaction> recentTransactions = new ArrayList<>();
}
