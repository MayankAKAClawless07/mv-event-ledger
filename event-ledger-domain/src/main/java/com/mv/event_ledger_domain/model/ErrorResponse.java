package com.mv.event_ledger_domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Standard API error response.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Schema(description = "Time the error was produced.", example = "2026-05-15T14:02:11Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code.", example = "400")
    private int status;

    @Schema(description = "Short error label.", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error detail.", example = "amount must be greater than 0")
    private String message;

    @Schema(description = "Trace identifier propagated across services.", example = "8f7e3d9b06f94a3f9a76f5e0a4fd97d1")
    private String traceId;
}
