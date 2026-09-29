package com.company.andy.common.event.publish;

import com.company.andy.common.configuration.profile.DisableForIT;
import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.tracing.TracingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.company.andy.common.event.publish.DomainEventPublishStatus.*;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.Constants.MONGO_ID;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

// DAO for publishing DomainEvents

@Slf4j
@Component
@DisableForIT
@RequiredArgsConstructor
public class PublishingDomainEventDao {
    private static final int MAX_PUBLISH_COUNT = 3;
    private final MongoTemplate mongoTemplate;
    private final TracingService tracingService;

    // Before publishing to messaging middleware,
    // events are staged(saved) into database in the same transaction that handles business logic
    public void stage(List<DomainEvent> events) {
        requireNonNull(events, "DomainEvents must not be null.");
        String currentTraceParent = tracingService.currentTraceParent();
        List<PublishingDomainEvent> publishingDomainEvents = events.stream()
                .map((DomainEvent it) -> new PublishingDomainEvent(it, currentTraceParent)).toList();
        mongoTemplate.insertAll(publishingDomainEvents);
    }

    public List<PublishingDomainEvent> stagedEvents(String startId, int limit) {
        requireNonBlank(startId, "Start ID must not be blank.");

        Query query = query(where(PublishingDomainEvent.Fields.status).in(CREATED, PUBLISH_FAILED)
                .and(MONGO_ID).gt(startId)
                .and(PublishingDomainEvent.Fields.publishedCount).lt(MAX_PUBLISH_COUNT))
                .with(by(ASC, PublishingDomainEvent.Fields.raisedAt))
                .limit(limit);
        return mongoTemplate.find(query, PublishingDomainEvent.class);
    }

    public void successPublish(String eventId) {
        requireNonBlank(eventId, "DomainEvent ID must not be blank.");
        Query query = Query.query(where(MONGO_ID).is(eventId));
        Update update = new Update();
        update.set(PublishingDomainEvent.Fields.status, PUBLISH_SUCCEED.name()).inc(PublishingDomainEvent.Fields.publishedCount);
        mongoTemplate.updateFirst(query, update, PublishingDomainEvent.class);
    }

    public void failPublish(String eventId) {
        requireNonBlank(eventId, "DomainEvent ID must not be blank.");
        Query query = Query.query(where(MONGO_ID).is(eventId));
        Update update = new Update();
        update.set(PublishingDomainEvent.Fields.status, PUBLISH_FAILED.name()).inc(PublishingDomainEvent.Fields.publishedCount);
        mongoTemplate.updateFirst(query, update, PublishingDomainEvent.class);
    }

    public PublishingDomainEvent byId(String eventId) {
        requireNonBlank(eventId, "Event ID must not be blank.");
        Query query = Query.query(where(MONGO_ID).is(eventId));
        return mongoTemplate.findOne(query, PublishingDomainEvent.class);
    }
}
