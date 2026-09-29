package com.company.andy.feature.systemsettings.command;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.systemsettings.domain.SystemSettings;
import com.company.andy.feature.systemsettings.domain.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// CommandService handles Command objects and orchestrates the processing flow.
// CommandService should not contain business logic but delegate to AggregateRoot or DomainService.


@Slf4j
@Component
@RequiredArgsConstructor
public class SystemSettingsCommandService {
    private final SystemSettingsRepository systemSettingsRepository;

    @Transactional
    public void updateBaseSettings(UpdateSystemBaseSettingsCommand command, PlatformActor actor) {
        SystemSettings systemSettings = systemSettingsRepository.getSystemSettings();
        systemSettings.updateBaseSettings(command.baseSettings(), actor);
        systemSettingsRepository.save(systemSettings);
        log.info("System base settings updated.");
    }
}
