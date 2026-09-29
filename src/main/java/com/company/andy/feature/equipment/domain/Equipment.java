package com.company.andy.feature.equipment.domain;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.feature.equipment.domain.event.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.company.andy.feature.equipment.domain.Equipment.EQUIPMENT_COLLECTION;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

// All AggregateRoot objects should extend from AggregateRoot.

@Slf4j
@Getter
@FieldNameConstants // For accessing field names
@TypeAlias(EQUIPMENT_COLLECTION) // Use an explict type alias
@Document(EQUIPMENT_COLLECTION) // Explicitly set MongoDB collection name
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator) // For Jackson and MongoDB deserialization
public class Equipment extends AggregateRoot {
    public final static String EQUIPMENT_COLLECTION = "equipment";
    private String name;
    private EquipmentStatus status;
    private String holder;
    private long maintenanceRecordCount;
    private EquipmentEngine engine;

    public Equipment(String name, OrgActor actor) {
        requireNonBlank(name, "name must not be blank");
        requireNonNull(actor, "actor must not be null");

        super(newEquipmentId(), actor);
        this.name = name;
        this.engine = new EquipmentEngine("DEFAULT_ENGINE_MODEL");
        raiseEvent(new EquipmentCreatedEvent(this, actor));
    }

    public Equipment(
            String id,
            String name,
            String orgId,
            EquipmentEngine engine,
            PlatformActor actor) {
        requireNonBlank(id, "id must not be blank");
        requireNonBlank(name, "name must not be blank");
        requireNonBlank(orgId, "orgId must not be null");
        requireNonNull(engine, "engine must not be null");
        requireNonNull(actor, "actor must not be null");

        super(id, orgId, actor);
        this.name = name;
        this.engine = engine;
        raiseEvent(new EquipmentCreatedEvent(this, actor));
    }

    public static String newEquipmentId() {
        return "EQP" + newSnowflakeId(); // Generate ID in the code
    }

    public void updateName(String newName, Actor actor) {
        if (Objects.equals(newName, this.name)) {
            return;
        }
        this.name = newName;
        // call raiseEvent() for publishing DomainEvents
        raiseEvent(new EquipmentNameUpdatedEvent(name, this, actor));
    }

    public void updateHolder(String newHolder, Actor actor) {
        if (Objects.equals(this.holder, newHolder)) {
            return;
        }

        String oldHolder = this.holder;
        this.holder = newHolder;
        raiseEvent(new EquipmentHolderUpdatedEvent(oldHolder, newHolder, this, actor));
    }

    public void updateStatus(EquipmentStatus status, Actor actor) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        raiseEvent(new EquipmentStatusUpdatedEvent(this.status, this, actor));
    }

    public void startEngine(Actor actor) {
        this.engine.start();
    }

    public void onDelete(Actor actor) {
        raiseEvent(new EquipmentDeletedEvent(this, actor));
    }
}
