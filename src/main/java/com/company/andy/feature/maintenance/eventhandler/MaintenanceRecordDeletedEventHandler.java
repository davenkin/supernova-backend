package com.company.andy.feature.maintenance.eventhandler;

import com.company.andy.common.event.consume.AbstractEventHandler;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.utils.ExceptionSwallowRunner;
import com.company.andy.feature.equipment.domain.task.CountMaintenanceRecordsForEquipmentTask;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// All event handlers should extend from AbstractEventHandler.


@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceRecordDeletedEventHandler extends AbstractEventHandler<MaintenanceRecordDeletedEvent> {
    private final CountMaintenanceRecordsForEquipmentTask countMaintenanceRecordsForEquipmentTask;

    @Override
    public void handle(MaintenanceRecordDeletedEvent event, PlatformActor actor) {
        ExceptionSwallowRunner.run(() -> countMaintenanceRecordsForEquipmentTask.run(event.getEquipmentId()));
    }

    @Override
    public boolean isIdempotent() {
        // This handler can run multiple times safely
        return true;
    }
}
