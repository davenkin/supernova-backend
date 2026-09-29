package com.company.andy.feature.equipment.domain.event;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.DomainEventType;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.equipment.domain.Equipment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

// Demonstrates an intermediate class inheriting from DomainEvent,
// which itself serves as the parent for other DomainEvent classes.

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class EquipmentUpdatedEvent extends DomainEvent {
    private String equipmentId;

    protected EquipmentUpdatedEvent(DomainEventType type, Equipment equipment, Actor actor) {
        super(type, equipment, actor);
        this.equipmentId = equipment.getId();
    }
}
