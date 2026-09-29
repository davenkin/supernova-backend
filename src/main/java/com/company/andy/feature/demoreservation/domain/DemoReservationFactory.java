package com.company.andy.feature.demoreservation.domain;

import com.company.andy.common.model.actor.PlatformActor;
import org.springframework.stereotype.Component;

// Always use factories to create AggregateRoot,
// which makes the creation process of AggregateRoot more explicit.

@Component
public class DemoReservationFactory {
    public DemoReservation createDemoReservation(String mobileNumber, PlatformActor actor) {
        return new DemoReservation(mobileNumber, actor);
    }
}
