package com.company.andy.feature.equipment.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.company.andy.TestFixture.randomMemberActor;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EquipmentDomainServiceTest {

    @Mock // @Mock means it's a mocked object
    private EquipmentRepository equipmentRepository;

    @InjectMocks // @InjectMocks means automatically inject other @Mock objects into this object
    private EquipmentDomainService equipmentDomainService;

    @Test
    void should_update_name() {
        Mockito.when(equipmentRepository.existsByName(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Equipment equipment = new Equipment("name", randomMemberActor());

        equipmentDomainService.updateEquipmentName(equipment, "newName", randomMemberActor());

        assertEquals("newName", equipment.getName());
    }
}