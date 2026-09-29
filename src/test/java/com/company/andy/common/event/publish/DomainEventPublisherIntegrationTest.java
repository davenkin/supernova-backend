package com.company.andy.common.event.publish;

import com.company.andy.IntegrationTest;
import com.company.andy.TestFixture;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.domain.event.EquipmentCreatedEvent;
import com.company.andy.support.TestingDomainEventSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import static com.company.andy.common.event.DomainEventType.EQUIPMENT_CREATED_EVENT;
import static com.company.andy.common.event.publish.DomainEventPublishStatus.*;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomEquipmentName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DomainEventPublisherIntegrationTest extends IntegrationTest {

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    @Autowired
    private EquipmentCommandService equipmentCommandService;

    @Autowired
    private PublishingDomainEventDao publishingDomainEventDao;

    @Autowired
    private TestingDomainEventSender domainEventSender;

    @Test
    void should_publish_domain_events() {
        OrgActor actor = TestFixture.randomMemberActor();
        String arId1 = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);
        String arId2 = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);
        String arId3 = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);
        String arId4 = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);

        EquipmentCreatedEvent event1 = latestDomainEventFor(arId1, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        EquipmentCreatedEvent event2 = latestDomainEventFor(arId2, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        EquipmentCreatedEvent event3 = latestDomainEventFor(arId3, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        EquipmentCreatedEvent event4 = latestDomainEventFor(arId4, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        assertEquals(CREATED, publishingDomainEventDao.byId(event1.getId()).getStatus());
        assertEquals(CREATED, publishingDomainEventDao.byId(event2.getId()).getStatus());
        assertEquals(CREATED, publishingDomainEventDao.byId(event3.getId()).getStatus());
        assertEquals(CREATED, publishingDomainEventDao.byId(event4.getId()).getStatus());

        domainEventPublisher.publishStagedDomainEvents(500);

        assertEquals(PUBLISH_SUCCEED, publishingDomainEventDao.byId(event1.getId()).getStatus());
        assertEquals(PUBLISH_SUCCEED, publishingDomainEventDao.byId(event2.getId()).getStatus());
        assertEquals(PUBLISH_SUCCEED, publishingDomainEventDao.byId(event3.getId()).getStatus());
        assertEquals(PUBLISH_SUCCEED, publishingDomainEventDao.byId(event4.getId()).getStatus());

        assertTrue(domainEventSender.hasEvent(event1.getId()));
        assertTrue(domainEventSender.hasEvent(event2.getId()));
        assertTrue(domainEventSender.hasEvent(event3.getId()));
        assertTrue(domainEventSender.hasEvent(event4.getId()));
    }

    @Test
    void should_fail_publish_domain_events_with_max_of_3_attempts() {
        OrgActor actor = TestFixture.randomMemberActor();
        String arId = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);
        EquipmentCreatedEvent event = latestDomainEventFor(arId, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);
        domainEventSender.throwExceptionFor(event.getId());

        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent1 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_FAILED, publishingDomainEvent1.getStatus());
        assertEquals(1, publishingDomainEvent1.getPublishedCount());

        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent2 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_FAILED, publishingDomainEvent2.getStatus());
        assertEquals(2, publishingDomainEvent2.getPublishedCount());

        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent3 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_FAILED, publishingDomainEvent3.getStatus());
        assertEquals(3, publishingDomainEvent3.getPublishedCount());

        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent4 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_FAILED, publishingDomainEvent4.getStatus());
        assertEquals(3, publishingDomainEvent4.getPublishedCount());
        domainEventSender.removeExceptionFor(event.getId());
    }

    @Test
    void should_publish_successfully_if_sender_recovered() {
        OrgActor actor = TestFixture.randomMemberActor();
        String arId = equipmentCommandService.createEquipment(CreateEquipmentCommand.builder().name(randomEquipmentName()).build(), actor);
        EquipmentCreatedEvent event = latestDomainEventFor(arId, EQUIPMENT_CREATED_EVENT, EquipmentCreatedEvent.class);

        domainEventSender.throwExceptionFor(event.getId());
        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent1 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_FAILED, publishingDomainEvent1.getStatus());
        assertEquals(1, publishingDomainEvent1.getPublishedCount());

        domainEventSender.removeExceptionFor(event.getId()); // recover
        domainEventPublisher.publishStagedDomainEvents(500);
        PublishingDomainEvent publishingDomainEvent2 = publishingDomainEventDao.byId(event.getId());
        assertEquals(PUBLISH_SUCCEED, publishingDomainEvent2.getStatus());
        assertEquals(2, publishingDomainEvent2.getPublishedCount());
    }
}