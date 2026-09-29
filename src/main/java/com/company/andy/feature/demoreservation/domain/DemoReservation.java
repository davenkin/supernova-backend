package com.company.andy.feature.demoreservation.domain;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.demoreservation.domain.event.DemoReservationCreatedEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.company.andy.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.company.andy.feature.demoreservation.domain.DemoReservation.DEMO_RESERVATION_COLLECTION;
import static lombok.AccessLevel.PRIVATE;

// All AggregateRoot objects should extend from AggregateRoot.
// Demonstrates platform level objects.
// Platform level objects are those without an orgId hence does not belong to any org.

@Slf4j
@Getter
@FieldNameConstants
@TypeAlias(DEMO_RESERVATION_COLLECTION)
@Document(DEMO_RESERVATION_COLLECTION)
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class DemoReservation extends AggregateRoot {
    public final static String DEMO_RESERVATION_COLLECTION = "demo_reservation";

    private String mobileNumber;

    public DemoReservation(String mobileNumber, PlatformActor actor) {
        super(newDemoReservationId(), actor);
        this.mobileNumber = mobileNumber;
        raiseEvent(new DemoReservationCreatedEvent(this, actor));
    }

    public static String newDemoReservationId() {
        return "DRV" + newSnowflakeId();
    }

    @Override
    protected boolean isPlatformObject() {
        return true;
    }
}
