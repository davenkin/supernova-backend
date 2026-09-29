package com.company.andy.feature.systemsettings.domain.event;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.systemsettings.domain.BaseSettings;
import com.company.andy.feature.systemsettings.domain.SystemSettings;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.common.event.DomainEventType.SYSTEM_BASE_SETTINGS_UPDATED_EVENT;
import static lombok.AccessLevel.PRIVATE;

// All DomainEvents should extend from DomainEvent, directly or indirectly.


@Getter
@TypeAlias("SYSTEM_BASE_SETTINGS_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class SystemBaseSettingsUpdatedEvent extends DomainEvent {
    private BaseSettings oldSettings;
    private BaseSettings newSettings;

    public SystemBaseSettingsUpdatedEvent(BaseSettings oldSettings,
                                          SystemSettings systemSettings,
                                          Actor raisedBy) {
        super(SYSTEM_BASE_SETTINGS_UPDATED_EVENT, systemSettings, raisedBy);
        this.oldSettings = oldSettings;
        this.newSettings = systemSettings.getBaseSettings();
    }
}
