package com.company.andy.common.event.consume;

import com.company.andy.common.event.DomainEvent;

import java.time.Instant;

public record HandledEvent(DomainEvent event, Instant handledAt) {
}
