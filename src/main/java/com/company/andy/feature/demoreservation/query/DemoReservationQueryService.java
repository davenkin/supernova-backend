package com.company.andy.feature.demoreservation.query;

// QueryService is used for querying data, which represent the "Q" of CQRS,
// QueryService can call repositories or directly use MongoTemplate to query database.

import com.company.andy.common.model.AggregateRoot;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.feature.demoreservation.domain.DemoReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.company.andy.feature.demoreservation.domain.DemoReservation.DEMO_RESERVATION_COLLECTION;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class DemoReservationQueryService {
    private final MongoTemplate mongoTemplate;

    public PagedResponse<QPagedDemoReservation> pageDemoReservations(PageDemoReservationQuery query) {
        Criteria criteria = new Criteria();

        if (isNotBlank(query.getSearch())) {
            criteria.and(DemoReservation.Fields.mobileNumber).regex(query.getSearch());
        }

        Query mongoQuery = Query.query(criteria);
        mongoQuery.fields().include(
                DemoReservation.Fields.mobileNumber,
                AggregateRoot.Fields.createdAt);

        Pageable pageable = query.pageable();
        long count = mongoTemplate.count(mongoQuery, DemoReservation.class);
        if (count == 0) {
            return PagedResponse.empty(pageable);
        }

        List<QPagedDemoReservation> reservations = mongoTemplate.find(mongoQuery.with(pageable),
                QPagedDemoReservation.class,
                DEMO_RESERVATION_COLLECTION);
        return new PagedResponse<>(reservations, pageable, count);
    }
}
