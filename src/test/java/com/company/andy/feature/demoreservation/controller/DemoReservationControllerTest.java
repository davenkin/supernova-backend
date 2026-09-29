package com.company.andy.feature.demoreservation.controller;

import com.company.andy.IntegrationTest;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.common.utils.ResponseId;
import com.company.andy.feature.demoreservation.command.CreateDemoReservationCommand;
import com.company.andy.feature.demoreservation.command.DemoReservationCommandService;
import com.company.andy.feature.demoreservation.domain.DemoReservation;
import com.company.andy.feature.demoreservation.domain.DemoReservationRepository;
import com.company.andy.feature.demoreservation.domain.event.DemoReservationCreatedEvent;
import com.company.andy.feature.demoreservation.query.PageDemoReservationQuery;
import com.company.andy.feature.demoreservation.query.QPagedDemoReservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.util.stream.IntStream;

import static com.company.andy.TestFixture.randomAnonymousActor;
import static com.company.andy.TestFixture.randomSupervisorActor;
import static com.company.andy.common.event.DomainEventType.DEMO_RESERVATION_CREATED_EVENT;
import static com.company.andy.feature.demoreservation.DemoReservationTestFixture.randomDemoReservationCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DemoReservationControllerTest extends IntegrationTest {
    @Autowired
    private DemoReservationRepository demoReservationRepository;

    @Autowired
    private DemoReservationCommandService demoReservationCommandService;

    @Test
    void should_create_demo_reservation() {
        // Prepare
        CreateDemoReservationCommand command = randomDemoReservationCommand();

        // Execute
        ResponseId responseId = restTestClient.post()
                .uri("/platform/demo-reservations")
                .body(command)
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody();

        // Verify
        DemoReservation reservation = demoReservationRepository.byId(responseId.id());
        assertEquals(command.mobileNumber(), reservation.getMobileNumber());
        assertNull(reservation.getOrgId()); // DemoReservation does not belong to any org

        // Verify raised DomainEvent(s)
        DemoReservationCreatedEvent createdEvent = latestDomainEventFor(reservation.getId(),
                DEMO_RESERVATION_CREATED_EVENT,
                DemoReservationCreatedEvent.class);
        assertEquals(command.mobileNumber(), createdEvent.getMobileNumber());
    }

    @Test
    void should_page_demo_reservations() {
        // Prepare
        PlatformActor anonymousActor = randomAnonymousActor();
        IntStream.range(0, 20)
                .forEach(_ -> demoReservationCommandService.createDemoReservation(randomDemoReservationCommand(), anonymousActor));

        // Execute
        PageDemoReservationQuery query = PageDemoReservationQuery.builder().pageSize(12).build();
        PagedResponse<QPagedDemoReservation> response = restTestClient.post()
                .uri("/platform/demo-reservations/paged").headers(authHeaderOf(randomSupervisorActor()))
                .body(query)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PagedResponse<QPagedDemoReservation>>() {
                }).returnResult().getResponseBody();

        // Verify
        assertEquals(12, response.content().size());
    }
}