package com.company.andy.feature.equipment.eventhandler.external;

import com.company.andy.common.event.consume.external.ExternalEvent;
import com.company.andy.feature.equipment.domain.EquipmentEngine;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PRIVATE;

// Demonstrates how to handle other event types other than DomainEvents

@Getter
@SuperBuilder
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class ExternalEquipmentInstalledEvent extends ExternalEvent {
    private String equipmentId;
    private String orgId;
    private String name;
    private EquipmentEngine engine;
}
