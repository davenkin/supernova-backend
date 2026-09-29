package com.company.andy.feature.systemsettings.command;

import com.company.andy.feature.systemsettings.domain.BaseSettings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

// Command objects are the mutation data you send to the controller,
// which usually results in state changes in the system.

@Builder
public record UpdateSystemBaseSettingsCommand(
        @Schema(description = "Base settings")
        @NotNull @Valid BaseSettings baseSettings) {
}
