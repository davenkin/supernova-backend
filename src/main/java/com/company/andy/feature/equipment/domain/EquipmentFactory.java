package com.company.andy.feature.equipment.domain;

import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Always use factories to create AggregateRoot,
// which makes the creation process of AggregateRoot more explicit.

@Component
@RequiredArgsConstructor
public class EquipmentFactory {

    public Equipment create(String name, OrgActor actor) {
        return new Equipment(name, actor);
    }

    public Equipment create(String equipmentId, String name, String orgId, EquipmentEngine engine, PlatformActor actor) {
        return new Equipment(equipmentId, name, orgId, engine, actor);
    }
}
