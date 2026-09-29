package com.company.andy.feature.maintenance.domain.task;

import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

// Tasks are one-time operation which operates on single or multiple objects.
// Task can use either Repository or MongoTemplate to access database.


@Slf4j
@Component
@RequiredArgsConstructor
public class SyncEquipmentNameToMaintenanceRecordsTask {
    private final MongoTemplate mongoTemplate;
    private final EquipmentRepository equipmentRepository;

    public void run(String equipmentId) {
        requireNonBlank(equipmentId, "equipmentId must not be blank");

        equipmentRepository.byIdOptional(equipmentId).ifPresent(equipment -> {
            Query query = new Query(where(MaintenanceRecord.Fields.equipmentId).is(equipmentId));
            Update update = new Update().set(MaintenanceRecord.Fields.equipmentName, equipment.getName());
            mongoTemplate.updateMulti(query, update, MaintenanceRecord.class);
            log.info("Synced equipment[{}] name to all maintenance records.", equipment.getId());
        });
    }
}
