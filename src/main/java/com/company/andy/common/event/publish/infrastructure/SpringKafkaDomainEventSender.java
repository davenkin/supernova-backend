package com.company.andy.common.event.publish.infrastructure;

import com.company.andy.common.configuration.profile.DisableForIT;
import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.publish.DomainEventSender;
import com.company.andy.common.event.publish.PublishingDomainEvent;
import com.company.andy.common.tracing.TracingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.company.andy.common.utils.Constants.KAFKA_DOMAIN_EVENT_TOPIC;

// Send DomainEvents to Kafka
// This is the only place where event publishing touches Kafka, hence the coupling to Kafka is minimized
@Slf4j
@Component
@NullMarked
@DisableForIT
@RequiredArgsConstructor
public class SpringKafkaDomainEventSender implements DomainEventSender {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final TracingService tracingService;

    @Override
    public CompletableFuture<String> send(PublishingDomainEvent publishingDomainEvent) {
        return this.tracingService.withRestoredTrace(publishingDomainEvent.getTraceparent(),
                "domain.event.send",
                () -> this.doSend(publishingDomainEvent.getEvent()));
    }

    private CompletableFuture<String> doSend(DomainEvent event) {
        return this.kafkaTemplate.send(KAFKA_DOMAIN_EVENT_TOPIC, event.getArId(), event)
                .thenApply(record -> record.getProducerRecord().value().getId());
    }
}
