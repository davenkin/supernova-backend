package com.company.andy.feature.demoreservation.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

// Query response object's name should start with "Q".

@Builder
@Schema(description = "Paged demo reservation")
public record QPagedDemoReservation(
        @Schema(description = "ID of the demo reservation")
        String id,
        @Schema(description = "Mobile number of the demo reservation")
        String mobileNumber,
        @Schema(description = "Create time of the demo reservation")
        Instant createdAt) {
}
