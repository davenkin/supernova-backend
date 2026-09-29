package com.company.andy.feature.equipment.eventhandler;

import com.company.andy.common.event.consume.AbstractEventHandler;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.event.EquipmentStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// All event handlers should extend from AbstractEventHandler.

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentStatusUpdatedEventHandler extends AbstractEventHandler<EquipmentStatusUpdatedEvent> {

    @Override
    public void handle(EquipmentStatusUpdatedEvent event, PlatformActor actor) {
        log.info("{} called for Equipment[{}].", this.getClass().getSimpleName(), event.getArId());
    }
}
