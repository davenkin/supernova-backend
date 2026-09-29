package com.company.andy.feature.equipment.domain.event;

import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.equipment.domain.Equipment;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.common.event.DomainEventType.EQUIPMENT_NAME_UPDATED_EVENT;
import static lombok.AccessLevel.PRIVATE;

// Demonstrates DomainEvents that inherits from another DomainEvent which further inherits from DomainEvent.

@Getter
@TypeAlias("EQUIPMENT_NAME_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class EquipmentNameUpdatedEvent extends EquipmentUpdatedEvent {
    private String updatedName;

    public EquipmentNameUpdatedEvent(String updatedName, Equipment equipment, Actor actor) {
        super(EQUIPMENT_NAME_UPDATED_EVENT, equipment, actor);
        this.updatedName = updatedName;
    }
}
