package com.company.andy.feature.systemsettings.domain;

import com.company.andy.common.model.actor.PlatformActor;
import org.springframework.stereotype.Component;

// Always use factories to create AggregateRoot,
// which makes the creation process of AggregateRoot more explicit.

@Component
public class SystemSettingsFactory {
    public SystemSettings createSystemSettings(PlatformActor actor) {
        return new SystemSettings(actor);
    }
}
