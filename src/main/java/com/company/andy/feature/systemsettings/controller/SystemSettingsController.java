package com.company.andy.feature.systemsettings.controller;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.systemsettings.command.SystemSettingsCommandService;
import com.company.andy.feature.systemsettings.command.UpdateSystemBaseSettingsCommand;
import com.company.andy.feature.systemsettings.query.QSystemSettings;
import com.company.andy.feature.systemsettings.query.SystemSettingsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Controller should be thin and calls into CommandService and QueryService.
// Controller should pass through the Actor to CommandService and QueryService.
// Actor should be either OrgActor or PlatformActor according to which API plane you are handling.
// Here SystemSettingsController belongs to the Platform API plane so PlatformActor should be used.

@Profile("local | it-embedded | it-local")
@Tag(name = "SystemSettingsController", description = "System settings APIs")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/platform/system-settings")
public class SystemSettingsController {
    private final SystemSettingsCommandService systemSettingsCommandService;
    private final SystemSettingsQueryService systemSettingsQueryService;

    @Operation(summary = "Get system settings")
    @GetMapping
    public QSystemSettings getSystemSettings() {
        return systemSettingsQueryService.getSystemSettings();
    }

    @Operation(summary = "Update system base settings")
    @PutMapping("/base-settings")
    public void updateBaseSetting(
            @RequestBody @Valid UpdateSystemBaseSettingsCommand command,
            @AuthenticationPrincipal @NotNull PlatformActor actor) {
        systemSettingsCommandService.updateBaseSettings(command, actor);
    }

}
