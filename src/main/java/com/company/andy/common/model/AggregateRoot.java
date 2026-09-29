package com.company.andy.common.model;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

// Base class for all AggregateRoot objects.

@Getter
@FieldNameConstants
@NoArgsConstructor(access = PROTECTED)
public abstract class AggregateRoot {
    private String id;

    // for org level object, orgId must not be null
    // for platform level object, orgId must be null
    private String orgId;

    // DomainEvents are stored temporarily in AggregateRoot.events for implementing "Transactional Outbox" pattern,
    // refer to: https://microservices.io/patterns/data/transactional-outbox.html.
    // Here @Transient is very important for not persisting events within the AggregateRoot.
    @Transient
    private List<DomainEvent> events;

    private Instant createdAt;

    // creator never changes even if the name of the creator changes,
    // if the name of the creator changes, no need to sync creator.name but instead you should sync creatorName
    private Actor creator;

    // duplicates with creator.id for easier query
    private String creatorId;

    // duplicates with creator.name for easier query,
    // when the name of the creator changes, creatorName should be synced with the latest value
    private String creatorName;

    @Version
    @Getter(PRIVATE)
    private Long _version; // for optimistic lock

    protected AggregateRoot(String id, OrgActor actor) {
        requireNonBlank(id, "id must not be blank.");
        requireNonNull(actor, "actor must not be null.");

        if (this.isPlatformObject()) {
            init(id, actor);
        } else {
            init(id, actor);
            this.orgId = actor.getOrgId();
        }
    }

    protected AggregateRoot(String id, PlatformActor actor) {
        requireNonBlank(id, "id must not be blank.");
        requireNonNull(actor, "actor must not be null.");

        if (!isPlatformObject()) {
            throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is not a platform level class.");
        }

        init(id, actor);
    }

    protected AggregateRoot(String id, String orgId, PlatformActor actor) {
        requireNonBlank(id, "id must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");
        requireNonNull(actor, "actor must not be null.");

        if (isPlatformObject()) {
            throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is not an org level class.");
        }

        init(id, actor);
        this.orgId = orgId;
    }

    private void init(String id, Actor actor) {
        this.id = id;
        this.createdAt = Instant.now();
        this.creator = actor;
        this.creatorId = actor.getId();
        this.creatorName = actor.getName();
    }


    // raiseEvent() only stores events in AggregateRoot temporarily,
    // the events will then be persisted into DB by Repository within the same transaction that saves the AggregateRoot object.
    // The actual sending of events to messaging middleware is handled by DomainEventPublisher.
    protected final void raiseEvent(DomainEvent event) {
        requireNonNull(event, "event must not be null.");
        requireNonNull(event.getType(), "event's type must not be null.");
        requireNonBlank(event.getArId(), "event's arId must not be null.");

        events().add(event);
    }

    private List<DomainEvent> events() {
        if (events == null) {
            this.events = new ArrayList<>();
        }

        return events;
    }

    public final void clearEvents() {
        this.events = null;
    }

    protected boolean isPlatformObject() {
        // An AggregateRoot object is either a platform object or an org object.
        // Platform object has no orgId, org object has orgId.
        // Default to false which means it's an org object.
        // Platform object should override this and return true.
        return false;
    }
}
