package com.company.andy.feature.demoreservation.controller;

import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.utils.PagedResponse;
import com.company.andy.common.utils.ResponseId;
import com.company.andy.feature.demoreservation.command.CreateDemoReservationCommand;
import com.company.andy.feature.demoreservation.command.DemoReservationCommandService;
import com.company.andy.feature.demoreservation.query.DemoReservationQueryService;
import com.company.andy.feature.demoreservation.query.PageDemoReservationQuery;
import com.company.andy.feature.demoreservation.query.QPagedDemoReservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.company.andy.common.model.actor.Actor.createAnonymousActor;
import static com.company.andy.common.model.actor.ActorOrigin.fromPlatformApiCall;
import static org.springframework.http.HttpStatus.CREATED;

// Controller should be thin and calls into CommandService and QueryService.
// Controller should pass through the Actor to CommandService and QueryService.
// Actor should be either OrgActor or PlatformActor according to which API plane you are handling.
// Here DemoReservationController belongs to the Platform API plane so PlatformActor should be used.


@Profile("local | it-embedded | it-local")
@Tag(name = "DemoReservationController", description = "Demo reservation APIs")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/platform/demo-reservations")
public class DemoReservationController {
    private final DemoReservationCommandService demoReservationCommandService;
    private final DemoReservationQueryService demoReservationQueryService;

    // This API is open for anyone, including anonymous users
    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Create a demo reservation")
    public ResponseId createDemoReservation(
            @RequestBody @Valid CreateDemoReservationCommand command,
            HttpServletRequest request) {
        PlatformActor actor = createAnonymousActor(fromPlatformApiCall(request));
        return new ResponseId(this.demoReservationCommandService.createDemoReservation(command, actor));
    }

    @Operation(summary = "Query demo reservations")
    @PostMapping("/paged")
    public PagedResponse<QPagedDemoReservation> pageDemoReservations(@RequestBody @Valid PageDemoReservationQuery query) {
        return this.demoReservationQueryService.pageDemoReservations(query);
    }
}
