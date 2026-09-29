package com.company.andy.common.event;

import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import static com.company.andy.common.event.consume.ConsumingEvent.Fields.consumedAt;
import static com.company.andy.common.event.publish.PublishingDomainEvent.Fields.raisedAt;
import static com.company.andy.common.utils.Constants.CONSUMING_EVENT_COLLECTION;
import static com.company.andy.common.utils.Constants.PUBLISHING_EVENT_COLLECTION;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

// This house keeping job removes old DomainEvents in DB both for publishing and consuming side, freeing more spaces for new events

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventHouseKeepingScheduledJob {
    private final MongoTemplate mongoTemplate;

    @Retryable(multiplier = 3, maxRetries = 3)
    public void removeOldPublishingDomainEvents(int olderThanDays) {
        log.info("Start remove old publishing DomainEvents from mongodb.");
        Query query = Query.query(where(raisedAt).lt(now().minus(olderThanDays, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, PUBLISHING_EVENT_COLLECTION);
        log.info("Removed {} old publishing DomainEvents which are more than {} days old.", result.getDeletedCount(), olderThanDays);
    }

    @Retryable(multiplier = 3, maxRetries = 3)
    public void removeOldConsumingDomainEvents(int olderThanDays) {
        log.info("Start remove old consuming DomainEvents from mongodb.");
        Query query = Query.query(where(consumedAt).lt(now().minus(olderThanDays, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, CONSUMING_EVENT_COLLECTION);
        log.info("Removed {} old consuming DomainEvents which are more than {} days old.", result.getDeletedCount(), olderThanDays);
    }
}
