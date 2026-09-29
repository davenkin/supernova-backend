package com.company.andy.feature.equipment.command;

import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentDomainService;
import com.company.andy.feature.equipment.domain.EquipmentFactory;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// CommandService handles Command objects and orchestrates the processing flow.
// CommandService should not contain business logic but delegate to AggregateRoot or DomainService.

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentCommandService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentFactory equipmentFactory;
    private final EquipmentDomainService equipmentDomainService;

    @Transactional
    public String createEquipment(CreateEquipmentCommand command, OrgActor actor) {
        Equipment equipment = equipmentFactory.create(command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Created Equipment[{}].", equipment.getId());
        return equipment.getId();
    }

    @Transactional
    public void updateEquipmentName(String equipmentId, UpdateEquipmentNameCommand command, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipmentDomainService.updateEquipmentName(equipment, command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Updated name for Equipment[{}].", equipment.getId());
    }

    @Transactional
    public void updateEquipmentHolder(String equipmentId, UpdateEquipmentHolderCommand command, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipment.updateHolder(command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Updated holder for Equipment[{}].", equipment.getId());
    }

    @Transactional
    public void deleteEquipment(String equipmentId, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipment.onDelete(actor);
        equipmentRepository.delete(equipment);
        log.info("Deleted Equipment[{}].", equipmentId);
    }
}
