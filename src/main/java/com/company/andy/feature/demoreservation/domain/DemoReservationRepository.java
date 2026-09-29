package com.company.andy.feature.demoreservation.domain;

import com.company.andy.common.mongo.AbstractMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// Only AggregateRoot objects have corresponding Repository.
// All repositories should extend AbstractMongoRepository.

@Repository
@RequiredArgsConstructor
public class DemoReservationRepository extends AbstractMongoRepository<DemoReservation> {
}
