package com.company.andy.common.event.consume;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.Constants.CONSUMING_EVENT_COLLECTION;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// Wrapper for all consumed events, it not only wraps DomainEvents but also wraps all types of consumed events
// You may add more information(such as if the event is redelivered etc.) if required, but should not be coupled to a specific messaging middleware

@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(CONSUMING_EVENT_COLLECTION)
@TypeAlias(CONSUMING_EVENT_COLLECTION)
public class ConsumingEvent {
    private String eventId;
    private String type;
    private String handler;
    private Instant consumedAt;
    private Object event;

    public ConsumingEvent(String eventId, Object event) {
        requireNonBlank(eventId, "Event ID must not be blank.");
        requireNonNull(event, "Event must not be null.");

        this.eventId = eventId;
        this.type = event.getClass().getName();
        this.event = event;
        this.handler = null; // init to null, later will be set to the handler's name
        this.consumedAt = Instant.now();
    }
}
