package com.company.andy.feature.demoreservation.command;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.ratelimiter.RateLimiter;
import com.company.andy.feature.demoreservation.domain.DemoReservation;
import com.company.andy.feature.demoreservation.domain.DemoReservationFactory;
import com.company.andy.feature.demoreservation.domain.DemoReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// CommandService handles Command objects and orchestrates the processing flow.
// CommandService should not contain business logic but delegate to AggregateRoot or DomainService.

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoReservationCommandService {
    private final DemoReservationFactory demoReservationFactory;
    private final DemoReservationRepository demoReservationRepository;
    private final RateLimiter rateLimiter;

    @Transactional
    public String createDemoReservation(CreateDemoReservationCommand command, PlatformActor actor) {
        rateLimiter.applyFor("create_demo_reservation", 5);
        DemoReservation demoReservation = demoReservationFactory.createDemoReservation(command.mobileNumber(), actor);
        demoReservationRepository.save(demoReservation);
        log.info("Created DemoReservation[{}].", demoReservation.getId());
        return demoReservation.getId();
    }
}
