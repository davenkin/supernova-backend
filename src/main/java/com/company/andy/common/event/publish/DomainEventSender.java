package com.company.andy.common.event.publish;

import java.util.concurrent.CompletableFuture;

// Send a DomainEvent to the messaging middleware
public interface DomainEventSender {
    CompletableFuture<String> send(PublishingDomainEvent publishingDomainEvent);
}
