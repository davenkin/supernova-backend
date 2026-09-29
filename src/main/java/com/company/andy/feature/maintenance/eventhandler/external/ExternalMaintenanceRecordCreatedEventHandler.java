package com.company.andy.feature.maintenance.eventhandler.external;

import com.company.andy.common.event.consume.AbstractEventHandler;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.company.andy.feature.maintenance.domain.MaintenanceRecordFactory;
import com.company.andy.feature.maintenance.domain.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Demonstrates how to handle other event types other than DomainEvents

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalMaintenanceRecordCreatedEventHandler extends AbstractEventHandler<ExternalMaintenanceRecordCreatedEvent> {
    private final EquipmentRepository equipmentRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final MaintenanceRecordFactory maintenanceRecordFactory;

    @Override
    public void handle(ExternalMaintenanceRecordCreatedEvent event, PlatformActor actor) {
        equipmentRepository.byIdOptional(event.getEquipmentId()).ifPresent(equipment -> {
            MaintenanceRecord record = maintenanceRecordFactory.createFromExternal(equipment,
                    event.getEquipmentStatus(),
                    event.getDescription(),
                    event.getChannelRecordId(),
                    actor);
            maintenanceRecordRepository.save(record);
        });
    }
}
