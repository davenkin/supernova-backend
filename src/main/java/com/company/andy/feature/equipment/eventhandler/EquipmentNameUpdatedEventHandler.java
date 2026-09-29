package com.company.andy.feature.equipment.eventhandler;

import com.company.andy.common.event.consume.AbstractEventHandler;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.utils.ExceptionSwallowRunner;
import com.company.andy.feature.equipment.domain.event.EquipmentNameUpdatedEvent;
import com.company.andy.feature.maintenance.domain.task.SyncEquipmentNameToMaintenanceRecordsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// All event handlers should extend from AbstractEventHandler.

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentNameUpdatedEventHandler extends AbstractEventHandler<EquipmentNameUpdatedEvent> {
    private final SyncEquipmentNameToMaintenanceRecordsTask syncEquipmentNameToMaintenanceRecordsTask;

    @Override
    public void handle(EquipmentNameUpdatedEvent event, PlatformActor actor) {
        ExceptionSwallowRunner.run(() -> syncEquipmentNameToMaintenanceRecordsTask.run(event.getEquipmentId()));
    }

    @Override
    public boolean isIdempotent() {
        // This handler can run multiple times safely
        return true;
    }

    @Override
    public boolean isTransactional() {
        // Not transactional as it updates multiple records which can exceed Mongo's transaction restrictions
        return false;
    }
}
