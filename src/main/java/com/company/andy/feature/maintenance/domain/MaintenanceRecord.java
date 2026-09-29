package com.company.andy.feature.maintenance.domain;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentStatus;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordCreatedEvent;
import com.company.andy.feature.maintenance.domain.event.MaintenanceRecordDeletedEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.company.andy.feature.maintenance.domain.MaintenanceRecord.MAINTENANCE_RECORD_COLLECTION;
import static com.company.andy.feature.maintenance.domain.MaintenanceRecordChannel.EXTERNAL;
import static com.company.andy.feature.maintenance.domain.MaintenanceRecordChannel.INTERNAL;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// All AggregateRoot objects should extend from AggregateRoot.

@Slf4j
@Getter
@FieldNameConstants
@TypeAlias(MAINTENANCE_RECORD_COLLECTION)
@Document(MAINTENANCE_RECORD_COLLECTION)
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class MaintenanceRecord extends AggregateRoot {
    public final static String MAINTENANCE_RECORD_COLLECTION = "maintenance-record";

    private String equipmentId;
    private String equipmentName;
    private EquipmentStatus status;
    private String description;
    private MaintenanceRecordChannel channel;
    private String channelRecordId;

    public MaintenanceRecord(
            Equipment equipment,
            EquipmentStatus status,
            String description,
            OrgActor actor) {
        requireNonNull(equipment, "equipment must not be null");
        requireNonNull(status, "status must not be null");
        requireNonBlank(description, "description must not be blank");
        requireNonNull(actor, "actor must not be null");

        super(newMaintenanceRecordId(), actor);
        this.equipmentId = equipment.getId();
        this.equipmentName = equipment.getName();
        this.status = status;
        this.description = description;
        this.channel = INTERNAL;
        raiseEvent(new MaintenanceRecordCreatedEvent(this, actor));
    }

    public MaintenanceRecord(
            Equipment equipment,
            EquipmentStatus status,
            String description,
            String channelRecordId,
            PlatformActor actor) {
        requireNonNull(equipment, "equipment must not be null");
        requireNonNull(status, "status must not be null");
        requireNonBlank(description, "description must not be blank");
        requireNonBlank(channelRecordId, "channelRecordId must not be blank");
        requireNonNull(actor, "actor must not be null");

        super(newMaintenanceRecordId(), equipment.getOrgId(), actor);
        this.equipmentId = equipment.getId();
        this.equipmentName = equipment.getName();
        this.status = status;
        this.description = description;
        this.channel = EXTERNAL;
        this.channelRecordId = channelRecordId;
        raiseEvent(new MaintenanceRecordCreatedEvent(this, actor));
    }

    public static String newMaintenanceRecordId() {
        return "MTR" + newSnowflakeId();
    }

    public void onDelete(Actor actor) {
        raiseEvent(new MaintenanceRecordDeletedEvent(this, actor));
    }
}
