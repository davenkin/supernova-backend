package com.company.andy.common.model;

import com.company.andy.common.model.actor.OrgActor;
import com.company.andy.common.model.actor.PlatformActor;
import org.junit.jupiter.api.Test;

import static com.company.andy.TestFixture.randomMemberActor;
import static com.company.andy.TestFixture.randomSupervisorActor;
import static org.junit.jupiter.api.Assertions.*;

class AggregateRootTest {

    @Test
    void should_create_org_ar_with_org_actor() {
        String id = "some-id";
        OrgActor actor = randomMemberActor();
        AggregateRoot ar = new AggregateRoot(id, actor) {
        };

        assertEquals(id, ar.getId());
        assertEquals(actor.getOrgId(), ar.getOrgId());
        assertNotNull(ar.getCreatedAt());
        assertEquals(actor, ar.getCreator());
        assertEquals(actor.getId(), ar.getCreatorId());
        assertEquals(actor.getName(), ar.getCreatorName());
    }

    @Test
    void should_create_platform_ar_with_org_actor() {
        String id = "some-id";
        OrgActor actor = randomMemberActor();
        AggregateRoot ar = new AggregateRoot(id, actor) {
            @Override
            protected boolean isPlatformObject() {
                return true;
            }
        };

        assertEquals(id, ar.getId());
        assertNull(ar.getOrgId());
        assertNotNull(ar.getCreatedAt());
        assertEquals(actor, ar.getCreator());
        assertEquals(actor.getId(), ar.getCreatorId());
        assertEquals(actor.getName(), ar.getCreatorName());
    }

    @Test
    void should_create_platform_ar_with_platform_actor() {
        String id = "some-id";
        PlatformActor actor = randomSupervisorActor();
        AggregateRoot ar = new AggregateRoot(id, actor) {
            @Override
            protected boolean isPlatformObject() {
                return true;
            }
        };

        assertEquals(id, ar.getId());
        assertNull(ar.getOrgId());
        assertNotNull(ar.getCreatedAt());
        assertEquals(actor, ar.getCreator());
        assertEquals(actor.getId(), ar.getCreatorId());
        assertEquals(actor.getName(), ar.getCreatorName());
    }

    @Test
    void should_not_create_org_ar_with_platform_actor_without_passed_org_id() {
        String id = "some-id";
        PlatformActor actor = randomSupervisorActor();
        assertThrows(UnsupportedOperationException.class, () -> new AggregateRoot(id, actor) {
        });
    }

    @Test
    void should_create_org_ar_with_platform_actor_with_passed_org_id() {
        String id = "some-id";
        String orgId = "some-org-id";
        PlatformActor actor = randomSupervisorActor();
        AggregateRoot ar = new AggregateRoot(id, orgId, actor) {
        };

        assertEquals(id, ar.getId());
        assertEquals(orgId, ar.getOrgId());
        assertNotNull(ar.getCreatedAt());
        assertEquals(actor, ar.getCreator());
        assertEquals(actor.getId(), ar.getCreatorId());
        assertEquals(actor.getName(), ar.getCreatorName());
    }

    @Test
    void should_not_create_platform_ar_with_platform_actor_with_passed_org_id() {
        String id = "some-id";
        String orgId = "some-org-id";
        PlatformActor actor = randomSupervisorActor();
        assertThrows(UnsupportedOperationException.class, () -> new AggregateRoot(id, orgId, actor) {
            @Override
            protected boolean isPlatformObject() {
                return true;
            }
        });
    }

}