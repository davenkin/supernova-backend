package com.company.andy.common.event.consume.infrastructure;

import com.company.andy.common.configuration.profile.DisableForIT;
import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.consume.EventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.company.andy.common.utils.Constants.KAFKA_DOMAIN_EVENT_TOPIC;

// Entry point for receiving events from Kafka
// This is the only place where event consuming touches Kafka, hence the coupling to Kafka is minimised

@Slf4j
@Component
@DisableForIT // Disable Kafka Listener for integration tests
@RequiredArgsConstructor
public class SpringKafkaEventListener {
    private final EventConsumer eventConsumer;

    // Listen to DomainEvents which are published by ourselves
    @KafkaListener(id = "domain-event-listener",
            groupId = "domain-event-listener",
            topics = {KAFKA_DOMAIN_EVENT_TOPIC},
            concurrency = "3")
    public void listenDomainEvent(DomainEvent event) {
        this.eventConsumer.consumeDomainEvent(event);
    }

    // You may add more @KafkaListener annotated methods for different topics if needed
}
