package com.company.andy.feature.maintenance.domain.task;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveOldMaintenanceRecordsTask {
    private final MongoTemplate mongoTemplate;

    // Remove MaintenanceRecords that are more than keepDays days old
    public void run(int keepDays) {
        log.info("Start removing maintenance records that are more than {} days old.", keepDays);
        if (keepDays <= 0) {
            throw new IllegalArgumentException("keepDays must be greater than 0");
        }
        Query query = Query.query(where(AggregateRoot.Fields.createdAt).lt(Instant.now().minus(keepDays, DAYS)));
        DeleteResult result = mongoTemplate.remove(query, MaintenanceRecord.class);
        log.info("Removed {} maintenance records that are more than {} days old.", keepDays, result.getDeletedCount());
    }
}
