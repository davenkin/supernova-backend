package com.company.andy.feature.maintenance.domain.event;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.common.event.DomainEventType.MAINTENANCE_RECORD_DELETED_EVENT;
import static lombok.AccessLevel.PRIVATE;

// All DomainEvents should extend from DomainEvent, directly or indirectly..


@Getter
@TypeAlias("MAINTENANCE_RECORD_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class MaintenanceRecordDeletedEvent extends DomainEvent {
    private String maintenanceRecordId;
    private String equipmentId;

    public MaintenanceRecordDeletedEvent(MaintenanceRecord record, Actor actor) {
        super(MAINTENANCE_RECORD_DELETED_EVENT, record, actor);
        this.maintenanceRecordId = record.getId();
        this.equipmentId = record.getEquipmentId();
    }
}
