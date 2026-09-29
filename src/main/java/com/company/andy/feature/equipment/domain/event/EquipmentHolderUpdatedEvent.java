package com.company.andy.feature.equipment.domain.event;

import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.equipment.domain.Equipment;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.common.event.DomainEventType.EQUIPMENT_HOLDER_UPDATED_EVENT;
import static lombok.AccessLevel.PRIVATE;

// Demonstrates DomainEvents that inherits from another DomainEvent which further inherits from DomainEvent.

@Getter
@TypeAlias("EQUIPMENT_HOLDER_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class EquipmentHolderUpdatedEvent extends EquipmentUpdatedEvent {
    private String oldHolder;
    private String newHolder;

    public EquipmentHolderUpdatedEvent(String oldHolder, String newHolder, Equipment equipment, Actor actor) {
        super(EQUIPMENT_HOLDER_UPDATED_EVENT, equipment, actor);
        this.oldHolder = oldHolder;
        this.newHolder = newHolder;
    }
}
