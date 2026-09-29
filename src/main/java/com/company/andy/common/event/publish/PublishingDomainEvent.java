package com.company.andy.common.event.publish;

import com.company.andy.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.company.andy.common.event.publish.DomainEventPublishStatus.CREATED;
import static com.company.andy.common.utils.Constants.PUBLISHING_EVENT_COLLECTION;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// Wrapper of DomainEvent for publishing, added "status" and "publishCount" to track the publishing process
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(PUBLISHING_EVENT_COLLECTION)
@TypeAlias(PUBLISHING_EVENT_COLLECTION)
public class PublishingDomainEvent {
    private String id;
    private DomainEvent event;
    private DomainEventPublishStatus status;
    private int publishedCount;
    private String traceparent;
    private Instant raisedAt;

    public PublishingDomainEvent(DomainEvent event, String traceparent) {
        requireNonNull(event, "DomainEvent must not be null.");

        this.id = event.getId();
        this.event = event;
        this.status = CREATED;
        this.publishedCount = 0;
        this.traceparent = traceparent;
        this.raisedAt = event.getRaisedAt();
    }
}
