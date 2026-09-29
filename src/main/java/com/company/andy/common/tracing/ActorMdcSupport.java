package com.company.andy.common.tracing;

import com.company.andy.common.model.actor.Actor;
import com.company.andy.common.model.actor.OrgActor;
import org.slf4j.MDC;

import java.util.List;

public class ActorMdcSupport {
    public static final List<String> ACTOR_MDC_KEYS = List.of(
            "actor",
            "actorOrgId",
            "actorOrigin"
    );

    public static void addMdc(Actor actor) {
        MDC.put("actor", "%s[%s]".formatted(actor.getPrincipalType(), actor.getId()));
        if (actor instanceof OrgActor orgActor) {
            MDC.put("actorOrgId", orgActor.getOrgId());
        }
        MDC.put("actorOrigin", actor.getOrigin().toString());
    }

    public static void clearMdc() {
        ACTOR_MDC_KEYS.forEach(MDC::remove);
    }

    public static void runWithMdc(Actor actor, Runnable runnable) {
        addMdc(actor);
        try {
            runnable.run();
        } finally {
            clearMdc();
        }
    }
}
