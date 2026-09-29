package com.company.andy.feature.equipment.scheduledjob;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.tracing.ActorMdcSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import static com.company.andy.common.model.actor.Actor.createScheduledJobActor;
import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

// Scheduler delegates work to ScheduledJob

@Slf4j
@Profile("local | it-embedded | it-local")
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class EquipmentJobScheduler {
    private final MaintenanceReminderScheduledJob maintenanceReminderScheduledJob;

    @Scheduled(cron = "0 10 2 1 * ?")
    @SchedulerLock(name = "remindForEquipmentMaintenance")
    public void remindForEquipmentMaintenance() {
        assertLocked();

        PlatformActor actor = createScheduledJobActor("remindForEquipmentMaintenance");
        ActorMdcSupport.runWithMdc(actor, () -> maintenanceReminderScheduledJob.run(actor));
    }
}
