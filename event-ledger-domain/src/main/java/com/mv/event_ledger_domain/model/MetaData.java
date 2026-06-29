package com.mv.event_ledger_domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Common optional metadata keys from upstream event sources.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaData {

    @Schema(description = "Name of the upstream source system.", example = "mainframe-batch")
    private String source;

    @Schema(description = "Batch identifier when the event is delivered as part of a batch.", example = "B-9042")
    private String batchId;
}
