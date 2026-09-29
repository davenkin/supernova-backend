package com.company.andy.common.infrastructure.transaction;

import com.company.andy.IntegrationTest;
import com.company.andy.common.exception.ServiceException;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.EquipmentCommandService;
import com.company.andy.feature.equipment.command.UpdateEquipmentHolderCommand;
import com.company.andy.feature.equipment.command.UpdateEquipmentNameCommand;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.equipment.domain.event.EquipmentNameUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.common.event.DomainEventType.EQUIPMENT_NAME_UPDATED_EVENT;
import static com.company.andy.feature.equipment.EquipmentTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionIntegrationTest extends IntegrationTest {

    @Autowired
    protected TestingTransactionService testingTransactionService;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private EquipmentCommandService equipmentCommandService;

    @Test
    void transaction_should_work_for_aggregate_root_and_domain_event() {
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        CreateEquipmentCommand createAnotherEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        equipmentCommandService.createEquipment(createAnotherEquipmentCommand, actor);

        UpdateEquipmentNameCommand updateEquipmentNameCommand = new UpdateEquipmentNameCommand(createAnotherEquipmentCommand.name());
        assertThrows(ServiceException.class,
                () -> equipmentCommandService.updateEquipmentName(equipmentId, updateEquipmentNameCommand, actor));

        assertEquals(createEquipmentCommand.name(), equipmentRepository.byId(equipmentId).getName());
        assertNull(latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT, EquipmentNameUpdatedEvent.class));
    }

    @Test
    void should_not_work_for_multiple_aggregate_roots_when_exception_thrown_with_transaction() {
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        UpdateEquipmentNameCommand updateEquipmentNameCommand = randomUpdateEquipmentNameCommand();
        UpdateEquipmentHolderCommand updateEquipmentHolderCommand = randomUpdateEquipmentHolderCommand();

        assertThrows(RuntimeException.class, () -> testingTransactionService.throwExceptionWithTransaction(equipmentId,
                updateEquipmentNameCommand,
                updateEquipmentHolderCommand, actor));

        Equipment dbEquipment = equipmentRepository.byId(equipmentId);
        assertNotEquals(updateEquipmentNameCommand.name(), dbEquipment.getName());
        assertNotEquals(updateEquipmentHolderCommand.name(), dbEquipment.getHolder());
        assertNull(latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT, EquipmentNameUpdatedEvent.class));
    }

    @Test
    void should_work_for_multiple_aggregate_roots_when_exception_thrown_at_the_end_without_transaction() {
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        UpdateEquipmentNameCommand updateEquipmentNameCommand = randomUpdateEquipmentNameCommand();
        UpdateEquipmentHolderCommand updateEquipmentHolderCommand = randomUpdateEquipmentHolderCommand();

        assertThrows(RuntimeException.class, () -> testingTransactionService.throwExceptionAtTheEndWithoutTransaction(equipmentId,
                updateEquipmentNameCommand,
                updateEquipmentHolderCommand, actor));

        Equipment dbEquipment = equipmentRepository.byId(equipmentId);
        assertEquals(updateEquipmentNameCommand.name(), dbEquipment.getName());
        assertEquals(updateEquipmentHolderCommand.name(), dbEquipment.getHolder());
        assertNotNull(latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT, EquipmentNameUpdatedEvent.class));
    }

    @Test
    void should_not_work_for_multiple_aggregate_roots_when_exception_thrown_in_the_middle_without_transaction() {
        OrgActor actor = randomMemberActor();
        CreateEquipmentCommand createEquipmentCommand = randomCreateEquipmentCommand();
        String equipmentId = equipmentCommandService.createEquipment(createEquipmentCommand, actor);
        UpdateEquipmentNameCommand updateEquipmentNameCommand = randomUpdateEquipmentNameCommand();
        UpdateEquipmentHolderCommand updateEquipmentHolderCommand = randomUpdateEquipmentHolderCommand();

        assertThrows(RuntimeException.class, () -> testingTransactionService.throwExceptionInTheMiddleWithoutTransaction(equipmentId,
                updateEquipmentNameCommand,
                updateEquipmentHolderCommand, actor));

        Equipment dbEquipment = equipmentRepository.byId(equipmentId);
        assertEquals(updateEquipmentNameCommand.name(), dbEquipment.getName());
        assertNotEquals(updateEquipmentHolderCommand.name(), dbEquipment.getHolder());
        assertNotNull(latestDomainEventFor(equipmentId, EQUIPMENT_NAME_UPDATED_EVENT, EquipmentNameUpdatedEvent.class));
    }
}