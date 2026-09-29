package com.company.andy.feature.maintenance.scheduledjob;

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

@Slf4j
@Profile("local | it-embedded | it-local")
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class MaintenanceRecordJobScheduler {
    private final RemoveOldMaintenanceRecordsScheduledJob removeOldMaintenanceRecordsScheduledJob;

    @Scheduled(cron = "0 0 2 1 * ?")
    @SchedulerLock(name = "removeOldMaintenanceRecords")
    public void removeOldMaintenanceRecords() {
        assertLocked();

        PlatformActor actor = createScheduledJobActor("removeOldMaintenanceRecords");
        ActorMdcSupport.runWithMdc(actor, () -> this.removeOldMaintenanceRecordsScheduledJob.run(actor));
    }
}
