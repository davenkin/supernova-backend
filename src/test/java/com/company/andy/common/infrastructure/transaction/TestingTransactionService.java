package com.company.andy.common.infrastructure.transaction;

import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.command.UpdateEquipmentHolderCommand;
import com.company.andy.feature.equipment.command.UpdateEquipmentNameCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestingTransactionService {
    private final EquipmentCommandService equipmentCommandService;

    @Transactional
    public void throwExceptionWithTransaction(
            String equipmentId,
            UpdateEquipmentNameCommand nameCommand,
            UpdateEquipmentHolderCommand holderCommand,
            OrgActor actor) {
        equipmentCommandService.updateEquipmentName(equipmentId, nameCommand, actor);
        equipmentCommandService.updateEquipmentHolder(equipmentId, holderCommand, actor);
        throw new RuntimeException("fake exception"); // mimic exception at the end of transaction flow
    }

    public void throwExceptionAtTheEndWithoutTransaction(
            String equipmentId,
            UpdateEquipmentNameCommand nameCommand,
            UpdateEquipmentHolderCommand holderCommand,
            OrgActor actor) {
        equipmentCommandService.updateEquipmentName(equipmentId, nameCommand, actor);
        equipmentCommandService.updateEquipmentHolder(equipmentId, holderCommand, actor);
        throw new RuntimeException("fake exception");
    }

    public void throwExceptionInTheMiddleWithoutTransaction(
            String equipmentId,
            UpdateEquipmentNameCommand nameCommand,
            UpdateEquipmentHolderCommand holderCommand,
            OrgActor actor) {
        equipmentCommandService.updateEquipmentName(equipmentId, nameCommand, actor);
        if (true) { // mimic exception in the middle of the transaction flow
            throw new RuntimeException("fake exception");
        }
        equipmentCommandService.updateEquipmentHolder(equipmentId, holderCommand, actor);
    }
}
