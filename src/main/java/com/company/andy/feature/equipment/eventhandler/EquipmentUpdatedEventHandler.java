package com.company.andy.feature.equipment.eventhandler;

import com.company.andy.common.event.consume.AbstractEventHandler;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.event.EquipmentUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Demonstrates handlers for handling parent events which covers all children events.
// Here EquipmentUpdatedEvent is the parent of EquipmentHolderUpdatedEvent and EquipmentNameUpdatedEvent.
// All children events will result in parent's handler to be called.

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentUpdatedEventHandler extends AbstractEventHandler<EquipmentUpdatedEvent> {

    @Override
    public void handle(EquipmentUpdatedEvent event, PlatformActor actor) {
        log.info("{} called for Equipment[{}].", this.getClass().getSimpleName(), event.getArId());
    }
}
