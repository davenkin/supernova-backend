package com.company.andy.feature.equipment.domain;

import com.company.andy.common.cache.CacheEvictor;
import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.mongo.AbstractMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.company.andy.common.model.AggregateRoot.Fields.createdAt;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.Constants.ORG_EQUIPMENTS_CACHE;
import static com.company.andy.feature.equipment.domain.Equipment.EQUIPMENT_COLLECTION;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

// Only AggregateRoot objects have corresponding Repository.
// All repositories should extend AbstractMongoRepository.

@Repository
@RequiredArgsConstructor
public class EquipmentRepository extends AbstractMongoRepository<Equipment> {
    private final CacheEvictor cacheEvictor;

    @Override
    public void save(Equipment equipment) {
        super.save(equipment);
        evictCachedEquipmentSummaries(equipment.getOrgId());
    }

    @Override
    public void save(List<Equipment> equipments) {
        super.save(equipments);
        equipments.stream().findFirst().ifPresent(it -> evictCachedEquipmentSummaries(it.getOrgId()));
    }

    @Override
    public void delete(Equipment equipment) {
        super.delete(equipment);
        evictCachedEquipmentSummaries(equipment.getOrgId());
    }

    @Override
    public void delete(List<Equipment> equipments) {
        super.delete(equipments);
        equipments.stream().findFirst().ifPresent(it -> evictCachedEquipmentSummaries(it.getOrgId()));
    }

    public boolean existsByName(String name, String orgId) {
        requireNonBlank(name, "name must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");

        Query query = query(where(Equipment.Fields.name).is(name).and(AggregateRoot.Fields.orgId).is(orgId));
        return mongoTemplate.exists(query, Equipment.class);
    }

    @Cacheable(value = ORG_EQUIPMENTS_CACHE, key = "#orgId")
    public CachedOrgEquipmentSummaries cachedEquipmentSummaries(String orgId) {
        requireNonBlank(orgId, "orgId must not be blank.");

        Query query = query(where(AggregateRoot.Fields.orgId).is(orgId)).with(by(ASC, createdAt));
        query.fields().include(AggregateRoot.Fields.orgId, Equipment.Fields.name, Equipment.Fields.status);
        return new CachedOrgEquipmentSummaries(mongoTemplate.find(query, EquipmentSummary.class, EQUIPMENT_COLLECTION));
    }

    public void evictCachedEquipmentSummaries(String orgId) {
        this.cacheEvictor.evict(ORG_EQUIPMENTS_CACHE, orgId);
    }
}
