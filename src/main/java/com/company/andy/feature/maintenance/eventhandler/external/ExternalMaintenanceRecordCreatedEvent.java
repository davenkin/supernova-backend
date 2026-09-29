package com.company.andy.feature.maintenance.eventhandler.external;

import com.company.andy.common.event.consume.external.ExternalEvent;
import com.company.andy.feature.equipment.domain.EquipmentStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PRIVATE;

// Demonstrates how to handle other event types other than DomainEvents

@Getter
@SuperBuilder
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class ExternalMaintenanceRecordCreatedEvent extends ExternalEvent {
    private String equipmentId;
    private String channelRecordId;
    private EquipmentStatus equipmentStatus;
    private String description;
}
