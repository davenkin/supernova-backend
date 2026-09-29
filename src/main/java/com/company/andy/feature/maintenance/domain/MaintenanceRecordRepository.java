package com.company.andy.feature.maintenance.domain;

import com.company.andy.common.mongo.AbstractMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

// Only AggregateRoot objects have corresponding Repository.
// All repositories should extend AbstractMongoRepository.

@Repository
@RequiredArgsConstructor
public class MaintenanceRecordRepository extends AbstractMongoRepository<MaintenanceRecord> {
    public Optional<MaintenanceRecord> latestForOptional(String equipmentId) {
        requireNonBlank(equipmentId, "equipmentId must not be blank.");

        Query query = Query.query(where(MaintenanceRecord.Fields.equipmentId).is(equipmentId));
        return Optional.ofNullable(mongoTemplate.findOne(query, MaintenanceRecord.class));
    }
}
