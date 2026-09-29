package com.company.andy.feature.maintenance.query;

import com.company.andy.common.exception.ServiceException;
import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.company.andy.common.exception.ErrorCode.MAINTENANCE_RECORD_NOT_FOUND;
import static com.company.andy.common.utils.Constants.ID;
import static com.company.andy.common.utils.Constants.MONGO_ID;
import static com.company.andy.common.utils.NullableMapUtils.mapOf;
import static com.company.andy.feature.maintenance.domain.MaintenanceRecord.MAINTENANCE_RECORD_COLLECTION;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

// QueryService is used for querying data, which represent the "Q" of CQRS,
// QueryService can call repositories or directly use MongoTemplate to query database.

@Component
@RequiredArgsConstructor
public class MaintenanceRecordQueryService {
    private final MongoTemplate mongoTemplate;

    public PagedResponse<QPagedMaintenanceRecord> pageMaintenanceRecords(PageMaintenanceRecordsQuery query, OrgActor actor) {
        Criteria criteria = where(AggregateRoot.Fields.orgId).is(actor.getOrgId());

        if (isNotBlank(query.getSearch())) {
            criteria.orOperator(where(MaintenanceRecord.Fields.equipmentName).regex(query.getSearch()),
                    where(MaintenanceRecord.Fields.description).regex(query.getSearch()));
        }

        if (query.getStatus() != null) {
            criteria.and(MaintenanceRecord.Fields.status).is(query.getStatus());
        }

        Query mongoQuery = Query.query(criteria);
        mongoQuery.fields().include(
                MaintenanceRecord.Fields.equipmentId,
                MaintenanceRecord.Fields.equipmentName,
                MaintenanceRecord.Fields.status,
                AggregateRoot.Fields.orgId,
                AggregateRoot.Fields.createdAt,
                AggregateRoot.Fields.creatorId);

        Pageable pageable = query.pageable();
        long count = mongoTemplate.count(mongoQuery, MaintenanceRecord.class);
        if (count == 0) {
            return PagedResponse.empty(pageable);
        }

        List<QPagedMaintenanceRecord> records = mongoTemplate.find(mongoQuery.with(pageable), QPagedMaintenanceRecord.class,
                MAINTENANCE_RECORD_COLLECTION);
        return new PagedResponse<>(records, pageable, count);
    }

    public QDetailedMaintenanceRecord getMaintenanceRecordDetail(String maintenanceRecordId, OrgActor actor) {
        Query query = Query.query(where(MONGO_ID).is(maintenanceRecordId).and(AggregateRoot.Fields.orgId).is(actor.getOrgId()));

        query.fields().include(
                MaintenanceRecord.Fields.equipmentId,
                MaintenanceRecord.Fields.equipmentName,
                MaintenanceRecord.Fields.status,
                MaintenanceRecord.Fields.description,
                AggregateRoot.Fields.orgId,
                AggregateRoot.Fields.createdAt,
                AggregateRoot.Fields.creatorId);
        QDetailedMaintenanceRecord record = mongoTemplate.findOne(query, QDetailedMaintenanceRecord.class, MAINTENANCE_RECORD_COLLECTION);

        if (record == null) {
            throw new ServiceException(MAINTENANCE_RECORD_NOT_FOUND, "Not found.",
                    mapOf(ID, maintenanceRecordId));
        }

        return record;
    }
}
