package com.company.andy.feature.maintenance.domain.task;

import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

// Tasks are one-time operation which operates on single or multiple objects.
// Task can use either Repository or MongoTemplate to access database.

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAllMaintenanceRecordsUnderEquipmentTask {
    private final MongoTemplate mongoTemplate;

    public void run(String equipmentId) {
        requireNonBlank(equipmentId, "equipmentId must not be blank");

        Query query = query(where(MaintenanceRecord.Fields.equipmentId).is(equipmentId));
        DeleteResult result = mongoTemplate.remove(query, MaintenanceRecord.class);
        log.info("Delete all {} maintenance records under equipment [{}].", result.getDeletedCount(), equipmentId);
    }
}
