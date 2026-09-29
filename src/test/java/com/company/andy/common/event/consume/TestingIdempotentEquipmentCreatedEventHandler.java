package com.company.andy.common.event.consume;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.event.EquipmentCreatedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Getter
@Order(3)
@Component
@RequiredArgsConstructor
public class TestingIdempotentEquipmentCreatedEventHandler extends AbstractEventHandler<EquipmentCreatedEvent> {
    private final List<HandledEvent> handledEvents = new CopyOnWriteArrayList<>();

    @Override
    public void handle(EquipmentCreatedEvent event, PlatformActor actor) {
        this.handledEvents.add(new HandledEvent(event, Instant.now()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
