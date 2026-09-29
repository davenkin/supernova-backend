package com.company.andy.feature.maintenance.controller;

import com.company.andy.IntegrationTest;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.common.utils.ResponseId;
import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.maintenance.command.CreateMaintenanceRecordCommand;
import com.company.andy.feature.maintenance.command.MaintenanceRecordCommandService;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.company.andy.feature.maintenance.domain.MaintenanceRecordRepository;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordCreatedEvent;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordDeletedEvent;
import com.company.andy.feature.maintenance.query.PageMaintenanceRecordsQuery;
import com.company.andy.feature.maintenance.query.QPagedMaintenanceRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.common.event.DomainEventType.MAINTENANCE_RECORD_CREATED_EVENT;
import static com.company.andy.common.event.DomainEventType.MAINTENANCE_RECORD_DELETED_EVENT;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomCreateEquipmentCommand;
import static com.company.andy.feature.maintenance.MaintenanceRecordTestFixture.randomCreateMaintenanceRecordCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaintenanceRecordControllerTest extends IntegrationTest {
    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentCommandService equipmentCommandService;

    @Autowired
    private MaintenanceRecordCommandService maintenanceRecordCommandService;

    @Test
    void should_create_maintenance_record() {
        // Prepare
        OrgActor actor = randomMemberActor();
        String equipmentId = equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);

        // Execute
        CreateMaintenanceRecordCommand createCommand = randomCreateMaintenanceRecordCommand(equipmentId);
        String maintenanceRecordId = restTestClient.post()
                .uri("/maintenance-records").headers(authHeaderOf(actor))
                .body(createCommand)
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody().id();

        // Verify
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.byId(maintenanceRecordId);
        assertEquals(equipmentId, maintenanceRecord.getEquipmentId());
        assertEquals(createCommand.status(), maintenanceRecord.getStatus());
        assertEquals(createCommand.description(), maintenanceRecord.getDescription());

        // Verify raised DomainEvent(s)
        MaintenanceRecordCreatedEvent createdEvent = latestDomainEventFor(maintenanceRecordId,
                MAINTENANCE_RECORD_CREATED_EVENT,
                MaintenanceRecordCreatedEvent.class);
        assertEquals(equipmentId, createdEvent.getEquipmentId());
    }

    @Test
    void created_maintenance_record_should_update_count_on_equipment() {
        // Prepare
        OrgActor actor = randomMemberActor();
        String equipmentId = equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);
        assertEquals(0, equipmentRepository.byId(equipmentId).getMaintenanceRecordCount());

        // Execute
        String maintenanceRecordId = restTestClient.post()
                .uri("/maintenance-records").headers(authHeaderOf(actor))
                .body(randomCreateMaintenanceRecordCommand(equipmentId))
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody().id();
        MaintenanceRecordCreatedEvent createdEvent = latestDomainEventFor(maintenanceRecordId,
                MAINTENANCE_RECORD_CREATED_EVENT,
                MaintenanceRecordCreatedEvent.class);
        eventConsumer.consumeDomainEvent(createdEvent);

        // Verify
        assertEquals(1, equipmentRepository.byId(equipmentId).getMaintenanceRecordCount());
    }

    @Test
    void should_delete_maintenance_record() {
        // Prepare
        OrgActor actor = randomMemberActor();
        String equipmentId = equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);
        String maintenanceRecordId = maintenanceRecordCommandService.createMaintenanceRecord(randomCreateMaintenanceRecordCommand(equipmentId),
                actor);
        MaintenanceRecordCreatedEvent createdEvent = latestDomainEventFor(maintenanceRecordId,
                MAINTENANCE_RECORD_CREATED_EVENT,
                MaintenanceRecordCreatedEvent.class);
        eventConsumer.consumeDomainEvent(createdEvent);
        assertEquals(1, equipmentRepository.byId(equipmentId).getMaintenanceRecordCount());

        // Execute
        restTestClient.delete().uri("/maintenance-records/{id}", maintenanceRecordId).headers(authHeaderOf(actor))
                .exchange().expectStatus().isOk();

        // Verify
        assertFalse(maintenanceRecordRepository.exists(maintenanceRecordId));

        // Verify raised DomainEvent(s)
        MaintenanceRecordDeletedEvent deletedEvent = latestDomainEventFor(maintenanceRecordId,
                MAINTENANCE_RECORD_DELETED_EVENT,
                MaintenanceRecordDeletedEvent.class);
        eventConsumer.consumeDomainEvent(deletedEvent);
        assertEquals(0, equipmentRepository.byId(equipmentId).getMaintenanceRecordCount());
    }

    @Test
    void should_page_maintenance_records() {
        // Prepare
        OrgActor actor = randomMemberActor();
        Consumer<HttpHeaders> authHeader = authHeaderOf(actor);
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        IntStream.range(0, 20)
                .forEach(_ -> maintenanceRecordCommandService.createMaintenanceRecord(randomCreateMaintenanceRecordCommand(equipmentId), actor));

        // Execute
        PageMaintenanceRecordsQuery query = PageMaintenanceRecordsQuery.builder().pageSize(12).build();
        PagedResponse<QPagedMaintenanceRecord> records = restTestClient.post()
                .uri("/maintenance-records/paged").headers(authHeader)
                .body(query)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<QPagedMaintenanceRecord>>() {
                }).returnResult().getResponseBody();

        // Verify
        assertEquals(12, records.content().size());
    }
}