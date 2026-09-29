package com.company.andy.common.event;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.demoreservation.domain.event.DemoReservationCreatedEvent;
import com.company.andy.feature.equipment.domain.event.*;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordCreatedEvent;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordDeletedEvent;
import com.company.andy.feature.systemsettings.domain.event.SystemBaseSettingsUpdatedEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

import static com.company.andy.common.utils.Constants.THIS_SERVICE_NAME;
import static com.company.andy.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @Type(value = EquipmentCreatedEvent.class, name = "EQUIPMENT_CREATED_EVENT"),
        @Type(value = EquipmentDeletedEvent.class, name = "EQUIPMENT_DELETED_EVENT"),
        @Type(value = EquipmentNameUpdatedEvent.class, name = "EQUIPMENT_NAME_UPDATED_EVENT"),
        @Type(value = EquipmentHolderUpdatedEvent.class, name = "EQUIPMENT_HOLDER_UPDATED_EVENT"),
        @Type(value = EquipmentStatusUpdatedEvent.class, name = "EQUIPMENT_STATUS_UPDATED_EVENT"),
        @Type(value = MaintenanceRecordCreatedEvent.class, name = "MAINTENANCE_RECORD_CREATED_EVENT"),
        @Type(value = MaintenanceRecordDeletedEvent.class, name = "MAINTENANCE_RECORD_DELETED_EVENT"),
        @Type(value = DemoReservationCreatedEvent.class, name = "DEMO_RESERVATION_CREATED_EVENT"),
        @Type(value = SystemBaseSettingsUpdatedEvent.class, name = "SYSTEM_BASE_SETTINGS_UPDATED_EVENT"),
})

// Base class for all DomainEvents
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PROTECTED)
public abstract class DomainEvent {
    private String source;
    private String id;
    private String arId;
    private String arOrgId;
    private DomainEventType type;
    private Instant raisedAt;
    private Actor raisedBy;

    protected DomainEvent(DomainEventType type, AggregateRoot ar, Actor raisedBy) {
        requireNonNull(type, "type must not be null.");
        requireNonNull(ar, "ar must not be null.");
        requireNonNull(raisedBy, "actor must not be null.");

        this.source = THIS_SERVICE_NAME;
        this.id = newEventId();
        this.arId = ar.getId();
        this.arOrgId = ar.getOrgId();
        this.type = type;
        this.raisedAt = Instant.now();
        this.raisedBy = raisedBy;
    }

    public static String newEventId() {
        return "EVT" + newSnowflakeId();
    }
}
