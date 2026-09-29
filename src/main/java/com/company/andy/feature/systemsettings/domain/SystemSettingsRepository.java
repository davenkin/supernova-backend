package com.company.andy.feature.systemsettings.domain;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.mongo.AbstractMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.company.andy.common.model.actor.Actor.createRobotActor;
import static com.company.andy.common.utils.Constants.SYSTEM_SETTINGS_CACHE;
import static com.company.andy.feature.systemsettings.domain.SystemSettings.SYSTEM_SETTINGS_ID;

// Only AggregateRoot objects have corresponding Repository.
// All repositories should extend AbstractMongoRepository.

@Repository
@RequiredArgsConstructor
public class SystemSettingsRepository extends AbstractMongoRepository<SystemSettings> {
    private final SystemSettingsFactory systemSettingsFactory;

    public SystemSettings getSystemSettings() {
        return byIdOptional(SYSTEM_SETTINGS_ID).orElseGet(() -> {
            PlatformActor actor = createRobotActor(getClass().getSimpleName());
            return systemSettingsFactory.createSystemSettings(actor);
        });
    }

    @Cacheable(value = SYSTEM_SETTINGS_CACHE, key = "'SYSTEM_SETTINGS'")
    public SystemSettings cachedSystemSettings() {
        return this.getSystemSettings();
    }

    @Override
    @CacheEvict(value = SYSTEM_SETTINGS_CACHE, allEntries = true)
    public void save(SystemSettings systemSettings) {
        super.save(systemSettings);
    }

    @Override
    public void save(List<SystemSettings> systemSettings) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void delete(SystemSettings systemSettings) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void delete(List<SystemSettings> systemSettings) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
