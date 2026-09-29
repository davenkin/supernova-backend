package com.company.andy.feature.equipment.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import static lombok.AccessLevel.PRIVATE;

// EquipmentEngine demonstrates an entity under the Equipment AggregateRoot

@Getter
@FieldNameConstants // For accessing field names
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator) // For Jackson and MongoDB deserialization
public class EquipmentEngine {
    private String model;
    private int temperature;
    private boolean started;

    public EquipmentEngine(String model) {
        this.model = model;
        this.temperature = 0;
        this.started = false;
    }

    public void start() {
        this.started = true;
        this.temperature = 100;
    }

    public void stop() {
        this.started = false;
        this.temperature = 0;
    }
}
