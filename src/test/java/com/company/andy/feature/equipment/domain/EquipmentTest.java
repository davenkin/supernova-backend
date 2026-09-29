package com.company.andy.feature.equipment.domain;

import com.company.andy.common.model.actor.OrgActor;
import org.junit.jupiter.api.Test;

import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.common.event.DomainEventType.EQUIPMENT_CREATED_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentTest {
    @Test
    void should_create_equipment() {
        OrgActor actor = randomMemberActor();
        Equipment equipment = new Equipment("name", actor);
        assertEquals("name", equipment.getName());
        assertEquals(1, equipment.getEvents().size());
        assertTrue(equipment.getEvents().stream()
                .anyMatch(domainEvent -> domainEvent.getType() == EQUIPMENT_CREATED_EVENT));
    }
}
