package com.company.andy.support;

import com.company.andy.common.configuration.profile.EnableOnlyForIT;
import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.publish.PublishingDomainEventDao;
import com.company.andy.common.tracing.TracingService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
@EnableOnlyForIT
public class TestingPublishingDomainEventDao extends PublishingDomainEventDao {
    private final static Set<String> serializationTestedDomainEvents = new HashSet<>();
    private final ObjectMapper objectMapper;

    public TestingPublishingDomainEventDao(
            MongoTemplate mongoTemplate,
            TracingService tracingService,
            ObjectMapper objectMapper) {
        super(mongoTemplate, tracingService);
        this.objectMapper = objectMapper;
    }

    @Override
    public void stage(List<DomainEvent> events) {
        super.stage(events);
        events.forEach(this::testJsonSerializationAndDeserialization);
    }

    // ensure every DomainEvent to be serializable and deserializable to/from JSON
    private void testJsonSerializationAndDeserialization(DomainEvent domainEvent) {
        String name = domainEvent.getClass().getName();
        if (!serializationTestedDomainEvents.contains(name)) {
            serializationTestedDomainEvents.add(name);
            String eventJson = objectMapper.writeValueAsString(domainEvent);
            DomainEvent deserialized = objectMapper.readValue(eventJson, DomainEvent.class);
            assertEquals(domainEvent.getClass(), deserialized.getClass());
        }
    }
}
