package com.company.andy.feature.systemsettings.controller;

import com.company.andy.IntegrationTest;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.systemsettings.command.UpdateSystemBaseSettingsCommand;
import com.company.andy.feature.systemsettings.domain.BaseSettings;
import com.company.andy.feature.systemsettings.domain.SystemSettings;
import com.company.andy.feature.systemsettings.domain.SystemSettingsRepository;
import com.company.andy.feature.systemsettings.query.QSystemSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.company.andy.TestFixture.randomEmail;
import static com.company.andy.TestFixture.randomSupervisorActor;
import static com.company.andy.common.utils.Constants.SYSTEM_SETTINGS_CACHE;
import static com.company.andy.feature.systemsettings.domain.SystemSettings.SYSTEM_SETTINGS_ID;
import static com.company.andy.support.PollingAssertion.pollAssert;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class SystemSettingsControllerTest extends IntegrationTest {

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @Test
    void should_get_system_settings() {
        PlatformActor actor = randomSupervisorActor();

        QSystemSettings qSystemSettings = restTestClient.get()
                .uri("/platform/system-settings").headers(authHeaderOf(actor))
                .exchange().expectStatus().isOk()
                .expectBody(QSystemSettings.class).returnResult().getResponseBody();
        assertNotNull(qSystemSettings);

        SystemSettings systemSettings = systemSettingsRepository.getSystemSettings();
        assertNull(systemSettings.getOrgId()); // SystemSettings does not belong to any org
    }

    @Test
    void should_update_system_base_settings() {
        // Prepare
        PlatformActor actor = randomSupervisorActor();
        UpdateSystemBaseSettingsCommand updateCommand = UpdateSystemBaseSettingsCommand.builder()
                .baseSettings(BaseSettings.builder().demoReservationNotificationEmails(List.of(randomEmail())).build())
                .build();

        // Execute
        restTestClient.put()
                .uri("/platform/system-settings/base-settings").headers(authHeaderOf(actor))
                .body(updateCommand)
                .exchange().expectStatus().isOk();

        // Verify
        assertEquals(updateCommand.baseSettings().demoReservationNotificationEmails(),
                systemSettingsRepository.getSystemSettings().getBaseSettings().demoReservationNotificationEmails());
    }

    @Test
    void save_system_settings_should_evict_cache() {
        // Prepare
        PlatformActor actor = randomSupervisorActor();
        QSystemSettings systemSettings = restTestClient.get()
                .uri("/platform/system-settings").headers(authHeaderOf(actor))
                .exchange().expectStatus().isOk()
                .expectBody(QSystemSettings.class).returnResult().getResponseBody();
        assertNotNull(systemSettings);
        pollAssert().run(() -> assertNotNull(cacheManager.getCache(SYSTEM_SETTINGS_CACHE).get(SYSTEM_SETTINGS_ID)));

        // Execute
        UpdateSystemBaseSettingsCommand updateCommand = UpdateSystemBaseSettingsCommand.builder()
                .baseSettings(BaseSettings.builder().demoReservationNotificationEmails(List.of(randomEmail())).build())
                .build();
        restTestClient.put()
                .uri("/platform/system-settings/base-settings").headers(authHeaderOf(actor))
                .body(updateCommand)
                .exchange().expectStatus().isOk();

        // Verify
        pollAssert().run(() -> assertNull(cacheManager.getCache(SYSTEM_SETTINGS_CACHE).get(SYSTEM_SETTINGS_ID)));
        QSystemSettings updatedCachedSettings = restTestClient.get()
                .uri("/platform/system-settings").headers(authHeaderOf(actor))
                .exchange().expectStatus().isOk()
                .expectBody(QSystemSettings.class).returnResult().getResponseBody();
        assertNotNull(updatedCachedSettings);
        pollAssert().run(() -> assertNotNull(cacheManager.getCache(SYSTEM_SETTINGS_CACHE).get(SYSTEM_SETTINGS_ID)));
    }
}