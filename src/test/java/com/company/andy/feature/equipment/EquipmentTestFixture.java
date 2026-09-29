package com.company.andy.feature.equipment;

import com.company.andy.feature.equipment.command.CreateEquipmentCommand;
import com.company.andy.feature.equipment.command.UpdateEquipmentHolderCommand;
import com.company.andy.feature.equipment.command.UpdateEquipmentNameCommand;
import com.company.andy.feature.equipment.domain.EquipmentStatus;
import org.apache.commons.lang3.RandomStringUtils;

import static com.company.andy.TestFixture.randomEnum;

public class EquipmentTestFixture {
    public static String randomEquipmentName() {
        return "EQUIPMENT_NAME_" + RandomStringUtils.secure().nextAlphanumeric(10);
    }

    public static String randomEquipmentHolderName() {
        return "HOLDER_NAME_" + RandomStringUtils.secure().nextAlphanumeric(10);
    }

    public static CreateEquipmentCommand randomCreateEquipmentCommand() {
        return new CreateEquipmentCommand(randomEquipmentName());
    }

    public static UpdateEquipmentNameCommand randomUpdateEquipmentNameCommand() {
        return new UpdateEquipmentNameCommand(randomEquipmentName());
    }

    public static UpdateEquipmentHolderCommand randomUpdateEquipmentHolderCommand() {
        return new UpdateEquipmentHolderCommand(randomEquipmentHolderName());
    }

    public static EquipmentStatus randomEquipmentStatus() {
        return randomEnum(EquipmentStatus.class);
    }
}
