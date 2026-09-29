package com.company.andy.feature.equipment.controller;

import com.company.andy.IntegrationTest;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.common.utils.ResponseId;
import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.command.UpdateEquipmentNameCommand;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.equipment.domain.EquipmentSummary;
import com.company.andy.feature.equipment.domain.event.EquipmentCreatedEvent;
import com.company.andy.feature.equipment.domain.event.EquipmentDeletedEvent;
import com.company.andy.feature.equipment.domain.event.EquipmentNameUpdatedEvent;
import com.company.andy.feature.equipment.query.PageEquipmentsQuery;
import com.company.andy.feature.equipment.query.QPagedEquipment;
import com.company.andy.feature.maintenance.command.MaintenanceRecordCommandService;
import com.company.andy.feature.maintenance.domain.MaintenanceRecordRepository;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.stream.IntStream;

import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.common.event.DomainEventType.*;
import static com.company.andy.common.utils.Constants.ORG_EQUIPMENTS_CACHE;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomCreateEquipmentCommand;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomUpdateEquipmentNameCommand;
import static com.company.andy.feature.maintenance.MaintenanceRecordTestFixture.randomCreateMaintenanceRecordCommand;
import static com.company.andy.support.PollingAssertion.pollAssert;
import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class EquipmentControllerTest extends IntegrationTest {
    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentCommandService equipmentCommandService;

    @Autowired
    private MaintenanceRecordCommandService maintenanceRecordCommandService;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Test
    void should_create_equipment() {
        // Prepare
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();

        // Execute
        ResponseId responseId = restTestClient.post()
                .uri("/equipments").headers(authHeaderOf(actor))
                .body(createEquipmentCommand)
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody();

        // Verify
        String equipmentId = responseId.id();
        Equipment equipment = equipmentRepository.byId(equipmentId);
        assertEquals(createEquipmentCommand.name(), equipment.getName());
        assertEquals(actor.getOrgId(), equipment.getOrgId());

        // Verify raised DomainEvent(s)
        EquipmentCreatedEvent equipmentCreatedEvent = latestDomainEventFor(equipmentId, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        assertEquals(equipmentId, equipmentCreatedEvent.getEquipmentId());
    }

    @Test
    void should_update_equipment_name() {
        // Prepare
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);

        // Execute
        UpdateEquipmentNameCommand updateEquipmentNameCommand = randomUpdateEquipmentNameCommand();
        restTestClient.put()
                .uri("/equipments/{id}/name", equipmentId).headers(authHeaderOf(actor))
                .body(updateEquipmentNameCommand)
                .exchange().expectStatus().isOk();

        // Verify
        Equipment equipment = equipmentRepository.byId(equipmentId);
        assertEquals(updateEquipmentNameCommand.name(), equipment.getName());

        // Verify raised DomainEvent(s)
        EquipmentNameUpdatedEvent equipmentNameUpdatedEvent = latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT,
                EquipmentNameUpdatedEvent.class);
        assertEquals(equipmentId, equipmentNameUpdatedEvent.getEquipmentId());
        assertEquals(updateEquipmentNameCommand.name(), equipmentNameUpdatedEvent.getUpdatedName());
    }

    @Test
    void update_equipment_name_should_also_sync_to_maintenance_records() {
        // Prepare
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        String maintenanceRecordId = maintenanceRecordCommandService.createMaintenanceRecord(randomCreateMaintenanceRecordCommand(equipmentId),
                actor);

        UpdateEquipmentNameCommand updateEquipmentNameCommand = randomUpdateEquipmentNameCommand();
        restTestClient.put()
                .uri("/equipments/{id}/name", equipmentId).headers(authHeaderOf(actor))
                .body(updateEquipmentNameCommand)
                .exchange().expectStatus().isOk();
        EquipmentNameUpdatedEvent equipmentNameUpdatedEvent = latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT,
                EquipmentNameUpdatedEvent.class);

        // Execute
        eventConsumer.consumeDomainEvent(equipmentNameUpdatedEvent);

        // Verify
        assertEquals(updateEquipmentNameCommand.name(), maintenanceRecordRepository.byId(maintenanceRecordId).getEquipmentName());
    }

    @Test
    void should_evict_org_equipment_summaries_cache_after_new_equipment_added() {
        // Prepare
        OrgActor actor = randomMemberActor();
        pollAssert().run(() -> assertNull(cacheManager.getCache(ORG_EQUIPMENTS_CACHE).get(actor.getOrgId())));
        equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);
        pollAssert().run(() -> assertNull(cacheManager.getCache(ORG_EQUIPMENTS_CACHE).get(actor.getOrgId())));
        List<EquipmentSummary> equipmentSummaries = equipmentRepository.cachedEquipmentSummaries(actor.getOrgId()).summaries();
        assertNotNull(equipmentSummaries);
        List<EquipmentSummary> cachedEquipmentSummaries = equipmentRepository.cachedEquipmentSummaries(actor.getOrgId()).summaries();
        assertNotNull(cachedEquipmentSummaries);
        pollAssert().run(() -> assertNotNull(cacheManager.getCache(ORG_EQUIPMENTS_CACHE).get(actor.getOrgId())));

        // Execute
        // Create another equipment to evict the cache
        restTestClient.post()
                .uri("/equipments").headers(authHeaderOf(actor))
                .body(randomCreateEquipmentCommand())
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody().id();

        // Verify
        pollAssert().run(() -> assertNull(cacheManager.getCache(ORG_EQUIPMENTS_CACHE).get(actor.getOrgId())));
    }

    @Test
    void should_delete_equipment() {
        // Prepare
        OrgActor actor = randomMemberActor();
        String equipmentId = equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);
        assertTrue(equipmentRepository.exists(equipmentId));

        // Execute
        restTestClient.delete().uri("/equipments/{id}", equipmentId).headers(authHeaderOf(actor)).exchange().expectStatus().isOk();

        // Verify
        assertFalse(equipmentRepository.exists(equipmentId));
        EquipmentDeletedEvent equipmentDeletedEvent = latestDomainEventFor(equipmentId, EQUIPMENT_DELETED_EVENT, EquipmentDeletedEvent.class);
        assertEquals(equipmentId, equipmentDeletedEvent.getEquipmentId());
    }

    @Test
    void delete_equipment_should_also_delete_its_maintenance_records() {
        // Prepare
        OrgActor actor = randomMemberActor();
        String equipmentId = equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor);
        String maintenanceRecordId = maintenanceRecordCommandService.createMaintenanceRecord(randomCreateMaintenanceRecordCommand(equipmentId),
                actor);
        assertTrue(maintenanceRecordRepository.exists(maintenanceRecordId));

        // Execute
        restTestClient.delete().uri("/equipments/{id}", equipmentId).headers(authHeaderOf(actor)).exchange().expectStatus().isOk();

        // Verify
        EquipmentDeletedEvent equipmentDeletedEvent = latestDomainEventFor(equipmentId, EQUIPMENT_DELETED_EVENT, EquipmentDeletedEvent.class);
        // Manually consume the event as Kafka is not enabled for integration tests
        eventConsumer.consumeDomainEvent(equipmentDeletedEvent);
        assertFalse(maintenanceRecordRepository.exists(maintenanceRecordId));
    }

    @Test
    void should_page_equipments() {
        // Prepare
        OrgActor actor = randomMemberActor();
        IntStream.range(0, 20).forEach(_ -> equipmentCommandService.createEquipment(randomCreateEquipmentCommand(), actor));

        // Execute
        PageEquipmentsQuery query = PageEquipmentsQuery.builder().pageSize(12).build();
        PagedResponse<QPagedEquipment> equipments = restTestClient.post()
                .uri("/equipments/paged").headers(authHeaderOf(actor))
                .body(query)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<QPagedEquipment>>() {
                }).returnResult().getResponseBody();

        // Verify
        assertEquals(12, equipments.content().size());
    }
}