package com.company.andy.common.security;

import com.company.andy.IntegrationTest;
import com.company.andy.common.exception.ApiError;
import com.company.andy.common.exception.QApiErrorResponse;
import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import com.company.andy.common.utils.ResponseId;
import com.company.andy.feature.demoreservation.query.PageDemoReservationQuery;
import com.company.andy.feature.equipment.domain.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.company.andy.TestFixture.*;
import static com.company.andy.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.company.andy.common.utils.Constants.ORG_ID_HEADER;
import static com.company.andy.feature.equipment.EquipmentTestFixture.randomCreateEquipmentCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecurityIntegrationTest extends IntegrationTest {
    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void should_throw_401_error_if_jwt_not_provided_for_org_api() {
        QApiErrorResponse response = restTestClient.post()
                .uri("/equipments")
                .body(randomCreateEquipmentCommand())
                .exchange().expectStatus().isUnauthorized()
                .expectBody(QApiErrorResponse.class).returnResult().getResponseBody();

        ApiError error = response.error();
        assertEquals(401, error.status());
        assertEquals(AUTHENTICATION_FAILED, error.code());
    }

    @Test
    void should_throw_401_error_if_jwt_not_provided_for_platform_api() {
        QApiErrorResponse response = restTestClient.post()
                .uri("/platform/demo-reservations/paged")
                .body(PageDemoReservationQuery.builder().pageSize(12).build())
                .exchange().expectStatus().isUnauthorized()
                .expectBody(QApiErrorResponse.class).returnResult().getResponseBody();

        ApiError error = response.error();
        assertEquals(401, error.status());
        assertEquals(AUTHENTICATION_FAILED, error.code());
    }

    @Test
    void supervisor_should_impersonate_org_actor_for_org_api() {
        PlatformActor actor = randomSupervisorActor();
        String orgId = randomOrgId();

        String equipmentId = restTestClient.post()
                .uri("/equipments")
                .headers(authHeaderOf(actor)).header(ORG_ID_HEADER, orgId)
                .body(randomCreateEquipmentCommand())
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody().id();

        assertEquals(orgId, equipmentRepository.byId(equipmentId).getOrgId());
    }

    @Test
    void platform_service_client_should_impersonate_org_actor_for_org_api() {
        PlatformActor actor = randomPlatformServiceClientActor();
        String orgId = randomOrgId();

        String equipmentId = restTestClient.post()
                .uri("/equipments")
                .headers(authHeaderOf(actor)).header(ORG_ID_HEADER, orgId)
                .body(randomCreateEquipmentCommand())
                .exchange().expectStatus().isCreated()
                .expectBody(ResponseId.class).returnResult().getResponseBody().id();

        assertEquals(orgId, equipmentRepository.byId(equipmentId).getOrgId());
    }

    @Test
    void supervisor_should_get_401_error_if_both_jwt_org_id_and_org_id_header_missing_for_org_api() {
        PlatformActor supervisorActor = randomSupervisorActor();

        restTestClient.post()
                .uri("/equipments")
                .headers(authHeaderOf(supervisorActor))
                .body(randomCreateEquipmentCommand())
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void org_actor_should_not_access_platform_api() {
        OrgActor orgActor = randomMemberActor();

        restTestClient.post()
                .uri("/platform/demo-reservations/paged")
                .headers(authHeaderOf(orgActor))
                .body(PageDemoReservationQuery.builder().pageSize(12).build())
                .exchange().expectStatus().isUnauthorized();
    }
}
