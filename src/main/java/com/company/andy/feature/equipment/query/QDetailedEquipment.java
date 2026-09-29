package com.company.andy.feature.equipment.query;

import com.company.andy.feature.equipment.domain.EquipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

// Query response object's name should start with "Q".

@Builder
@Schema(description = "Detailed equipment")
public record QDetailedEquipment(
        @Schema(description = "ID of the equipment")
        String id,
        @Schema(description = "Org ID of the equipment")
        String orgId,
        @Schema(description = "Name of the equipment")
        String name,
        @Schema(description = "Status of the equipment")
        EquipmentStatus status,
        @Schema(description = "Create time of the equipment")
        Instant createdAt,
        @Schema(description = "Creator of the equipment")
        String createdBy) {
}
