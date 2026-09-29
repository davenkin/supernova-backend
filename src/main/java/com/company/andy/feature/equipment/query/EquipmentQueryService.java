package com.company.andy.feature.equipment.query;

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.feature.equipment.domain.Equipment;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import com.company.andy.feature.equipment.domain.EquipmentSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.company.andy.feature.equipment.domain.Equipment.EQUIPMENT_COLLECTION;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

// QueryService is used for querying data, which represent the "Q" of CQRS,
// QueryService can call repositories or directly use MongoTemplate to query database.

@Component
@RequiredArgsConstructor
public class EquipmentQueryService {
    private final MongoTemplate mongoTemplate;
    private final EquipmentRepository equipmentRepository;

    public PagedResponse<QPagedEquipment> pageEquipments(PageEquipmentsQuery query, OrgActor actor) {
        Criteria criteria = where(AggregateRoot.Fields.orgId).is(actor.getOrgId());

        if (isNotBlank(query.getSearch())) {
            criteria.and(Equipment.Fields.name).regex(query.getSearch());
        }

        if (query.getStatus() != null) {
            criteria.and(Equipment.Fields.status).is(query.getStatus());
        }

        Query mongoQuery = Query.query(criteria);
        mongoQuery.fields().include(AggregateRoot.Fields.orgId,
                Equipment.Fields.name,
                Equipment.Fields.status,
                AggregateRoot.Fields.createdAt,
                AggregateRoot.Fields.creatorId);

        Pageable pageable = query.pageable();
        long count = mongoTemplate.count(mongoQuery, Equipment.class);
        if (count == 0) {
            return PagedResponse.empty(pageable);
        }

        List<QPagedEquipment> equipments = mongoTemplate.find(mongoQuery.with(pageable), QPagedEquipment.class, EQUIPMENT_COLLECTION);
        return new PagedResponse<>(equipments, pageable, count);
    }

    public QDetailedEquipment getEquipmentDetail(String equipmentId, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        return QDetailedEquipment.builder()
                .id(equipment.getId())
                .orgId(equipment.getOrgId())
                .name(equipment.getName())
                .status(equipment.getStatus())
                .createdAt(equipment.getCreatedAt())
                .createdBy(equipment.getCreatorId())
                .build();
    }

    public List<EquipmentSummary> getAllEquipmentSummaries(OrgActor actor) {
        return equipmentRepository.cachedEquipmentSummaries(actor.getOrgId()).summaries();
    }
}
