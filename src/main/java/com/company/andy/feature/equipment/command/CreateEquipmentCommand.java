package com.company.andy.feature.equipment.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// Command objects are the mutation data you send to the controller,
// which usually results in state changes in the system.

@Builder
public record CreateEquipmentCommand(
        @Schema(description = "Name of the equipment")
        @NotBlank @Size(max = 100) String name) {
}
