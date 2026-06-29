package com.mv.event_ledger_domain.model;

import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.util.RandomIdGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Transaction application request sent from the Gateway to Account Service.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransaction {

    @Schema(description = "Original gateway event identifier. Unique for idempotent account application.", example = "5af0e2f0a7c54ab99de682f4a6a2d1c9", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "eventId is required")
    @Pattern(regexp = "^[A-Za-z0-9]{8,80}$", message = "eventId must be an alphanumeric string between 8 and 80 characters")
    @Builder.Default
    private String eventId = RandomIdGenerator.randomEventId();

    @Schema(description = "Target random alphanumeric account identifier.", example = "9c0f6f1d2e3a4b5c8d7e6f5a4b3c2d1e", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "accountId is required")
    @Pattern(regexp = "^[A-Za-z0-9]{8,80}$", message = "accountId must be an alphanumeric string between 8 and 80 characters")
    @Builder.Default
    private String accountId = RandomIdGenerator.randomAccountId();

    @Schema(description = "Transaction type.", example = "CREDIT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "type is required")
    private Type type;

    @Schema(description = "Positive transaction amount.", example = "150.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "ISO 4217 currency code.", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter uppercase ISO code")
    private String currency;

    @Schema(description = "Original event occurrence time.", example = "2026-05-15T14:02:11Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "eventTimestamp is required")
    private Instant eventTimestamp;
}
