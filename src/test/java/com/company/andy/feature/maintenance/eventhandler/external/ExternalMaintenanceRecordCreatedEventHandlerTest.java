package com.company.andy.feature.maintenance.eventhandler.external;

import com.company.andy.IntegrationTest;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.equipment.domain.EquipmentStatus;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.company.andy.feature.maintenance.domain.MaintenanceRecordRepository;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.company.andy.TestFixture.randomExternalEventId;
import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.common.event.DomainEventType.MAINTENANCE_RECORD_CREATED_EVENT;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomCreateEquipmentCommand;
import static com.company.andy.feature.maintenance.domain.MaintenanceRecordChannel.EXTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalMaintenanceRecordCreatedEventHandlerTest extends IntegrationTest {
    @Autowired
    private EquipmentCommandService equipmentCommandService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Test
    void external_maintenance_record_created_event_should_be_added() {
        // Prepare
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);

        ExternalMaintenanceRecordCreatedEvent externalEvent = ExternalMaintenanceRecordCreatedEvent.builder()
                .eventId(randomExternalEventId())
                .eventType("MAINTENANCE_RECORD_CREATED")
                .channelRecordId(UUID.randomUUID().toString())
                .equipmentId(equipmentId)
                .equipmentStatus(EquipmentStatus.RUNNING)
                .description("This is a maintenance record from external system")
                .build();

        // Execute
        eventConsumer.consumeExternalEvent(externalEvent);

        // Verify
        Equipment equipment = equipmentRepository.byId(equipmentId);
        MaintenanceRecord record = maintenanceRecordRepository.latestForOptional(equipmentId).get();
        assertEquals(externalEvent.getChannelRecordId(), record.getChannelRecordId());
        assertEquals(EXTERNAL, record.getChannel());
        assertEquals(externalEvent.getEquipmentStatus(), record.getStatus());
        assertEquals(equipment.getName(), record.getEquipmentName());

        // Verify raised DomainEvent(s)
        MaintenanceRecordCreatedEvent internalEvent = latestDomainEventFor(record.getId(),
                MAINTENANCE_RECORD_CREATED_EVENT,
                MaintenanceRecordCreatedEvent.class);
        assertEquals(equipmentId, internalEvent.getEquipmentId());
    }
}

