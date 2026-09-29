package com.company.andy.feature.equipment.scheduledjob;

import com.company.andy.common.model.actor.PlatformActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// ScheduledJob is run by Scheduler

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceReminderScheduledJob {

    public void run(PlatformActor actor) {
        log.info("MaintenanceReminderScheduledJob started.");

        //do something

        log.info("MaintenanceReminderScheduledJob ended.");
    }
}
