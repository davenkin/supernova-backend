package com.company.andy.common.migration;

import com.company.andy.common.event.consume.ConsumingEvent;
import com.company.andy.common.event.publish.PublishingDomainEvent;
import com.company.andy.common.model.AggregateRoot;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.maintenance.domain.MaintenanceRecord;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static org.springframework.data.domain.Sort.Direction.DESC;

// Class name should follow format: "Migration[3 digits index]_[SimpleDescriptionOfYourMigration]"

@Slf4j
@ChangeUnit(id = "Migration002_BaseSetup", order = "002", author = "andy", transactional = false)
public class Migration002_BaseSetup {

    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        ensurePublishingDomainEventIndexes(mongoTemplate);
        ensureConsumingDomainEventIndexes(mongoTemplate);
        ensureEquipmentIndexes(mongoTemplate);
        ensureMaintenanceRecordIndexes(mongoTemplate);
    }

    private void ensurePublishingDomainEventIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(PublishingDomainEvent.class);
        indexOperations.createIndex(new Index().on(PublishingDomainEvent.Fields.status, DESC).named("idx_status"));
        indexOperations.createIndex(new Index().on(PublishingDomainEvent.Fields.publishedCount, DESC).named("idx_publishedCount"));
        indexOperations.createIndex(new Index().on(PublishingDomainEvent.Fields.raisedAt, DESC).named("idx_raisedAt"));
    }

    private void ensureConsumingDomainEventIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(ConsumingEvent.class);
        indexOperations.createIndex(new Index()
                .on(ConsumingEvent.Fields.eventId, DESC)
                .on(ConsumingEvent.Fields.handler, DESC)
                .named("idx_eventId_handler")
                .unique());
    }

    private void ensureEquipmentIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(Equipment.class);
        indexOperations.createIndex(new Index().on(Equipment.Fields.name, DESC).named("idx_name"));
        indexOperations.createIndex(new Index().on(Equipment.Fields.status, DESC).named("idx_status"));
        indexOperations.createIndex(new Index().on(AggregateRoot.Fields.orgId, DESC).named("idx_orgId"));
    }

    private void ensureMaintenanceRecordIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(MaintenanceRecord.class);
        indexOperations.createIndex(new Index().on(MaintenanceRecord.Fields.equipmentId, DESC).named("idx_equipmentId"));
        indexOperations.createIndex(new Index().on(MaintenanceRecord.Fields.equipmentName, DESC).named("idx_equipmentName"));
        indexOperations.createIndex(new Index().on(MaintenanceRecord.Fields.status, DESC).named("idx_status"));
        indexOperations.createIndex(new Index().on(AggregateRoot.Fields.orgId, DESC).named("idx_orgId"));
    }

    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
    }
}
