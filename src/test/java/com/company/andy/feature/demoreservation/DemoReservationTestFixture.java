package com.company.andy.feature.demoreservation;

import com.company.andy.feature.demoreservation.command.CreateDemoReservationCommand;

import static com.company.andy.TestFixture.randomMobileNumber;

public class DemoReservationTestFixture {
    public static CreateDemoReservationCommand randomDemoReservationCommand() {
        return new CreateDemoReservationCommand(randomMobileNumber());
    }
}
