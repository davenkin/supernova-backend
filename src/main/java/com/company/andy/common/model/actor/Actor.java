package com.company.andy.common.model.actor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.function.Consumer;

import static com.company.andy.common.model.actor.ActorOrigin.fromRobot;
import static com.company.andy.common.model.actor.ActorOrigin.fromScheduledJob;
import static com.company.andy.common.model.actor.PrincipalType.*;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = OrgActor.class, name = "ORG_ACTOR"),
        @JsonSubTypes.Type(value = PlatformActor.class, name = "PLATFORM_ACTOR"),
})

@Getter
@FieldNameConstants
@NoArgsConstructor(access = PROTECTED)
public abstract class Actor {
    private String id;
    private String name;
    private ActorType type;
    private ActorOrigin origin;
    private PrincipalType principalType;

    protected Actor(String id,
                    String name,
                    ActorType type,
                    PrincipalType principalType,
                    ActorOrigin origin) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.origin = origin;
        this.principalType = principalType;
    }

    public static OrgActor createMemberActor(String memberId,
                                             String name,
                                             String orgId,
                                             Set<OrgRole> roles,
                                             ActorOrigin origin) {
        requireNonBlank(memberId, "memberId must not be blank.");
        requireNonBlank(name, "name must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");
        requireNonNull(roles, "roles must not be null.");
        requireNonNull(origin, "origin must not be null.");
        return new OrgActor(memberId, name, memberId, orgId, roles, MEMBER, origin);
    }

    public static OrgActor createOrgServiceClientActor(String clientId,
                                                       String orgId,
                                                       ActorOrigin origin) {
        requireNonBlank(clientId, "clientId must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");
        requireNonNull(origin, "origin must not be null.");
        return new OrgActor(clientId, null, null, orgId, Set.of(), ORG_SERVICE_CLIENT, origin);
    }

    public static OrgActor createSupervisedOrgActor(String supervisorId,
                                                    String name,
                                                    String orgId,
                                                    ActorOrigin origin) {
        requireNonBlank(supervisorId, "supervisorId must not be blank.");
        requireNonBlank(name, "name must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");
        requireNonNull(origin, "origin must not be null.");
        return new OrgActor(supervisorId, name, null, orgId, Set.of(), SUPERVISOR, origin);
    }

    public static OrgActor createPlatformServiceClientOrgActor(String clientId,
                                                               String orgId,
                                                               ActorOrigin origin) {
        requireNonBlank(clientId, "clientId must not be blank.");
        requireNonBlank(orgId, "orgId must not be blank.");
        requireNonNull(origin, "origin must not be null.");
        return new OrgActor(clientId, null, null, orgId, Set.of(), PLATFORM_SERVICE_CLIENT, origin);
    }

    public static PlatformActor createSupervisorActor(String supervisorId, String name, Set<PlatformRole> roles, ActorOrigin origin) {
        requireNonBlank(supervisorId, "supervisorId must not be blank.");
        requireNonBlank(name, "name must not be blank.");
        requireNonNull(roles, "roles must not be null.");
        requireNonNull(origin, "origin must not be null.");
        return new PlatformActor(supervisorId, name, supervisorId, roles, SUPERVISOR, origin);
    }

    public static PlatformActor createPlatformServiceClientActor(String serviceClientId, ActorOrigin origin) {
        requireNonBlank(serviceClientId, "serviceClientId must not be blank.");
        requireNonNull(origin, "origin must not be null.");
        return new PlatformActor(serviceClientId, null, null, Set.of(), PLATFORM_SERVICE_CLIENT, origin);
    }

    public static PlatformActor createScheduledJobActor(String jobName) {
        requireNonBlank(jobName, "jobName must not be blank.");
        return new PlatformActor(jobName, null, null, Set.of(), ROBOT, fromScheduledJob(jobName));
    }

    public static PlatformActor createEventHandlerActor(String handlerName, ActorOrigin origin) {
        requireNonBlank(handlerName, "handlerName must not be blank.");
        requireNonNull(origin, "origin must not be null.");
        return new PlatformActor(handlerName, null, null, Set.of(), ROBOT, origin);
    }

    public static PlatformActor createRobotActor(String robotName) {
        requireNonBlank(robotName, "robotName must not be blank.");
        return new PlatformActor(robotName, null, null, Set.of(), ROBOT, fromRobot(robotName));
    }

    public static PlatformActor createAnonymousActor(ActorOrigin origin) {
        requireNonNull(origin, "origin must not be null.");
        return new PlatformActor(ANONYMOUS.name(), null, null, Set.of(), ANONYMOUS, origin);
    }

    public void ifOrgActor(Consumer<OrgActor> orgActorConsumer) {
        if (this instanceof OrgActor actor) {
            orgActorConsumer.accept(actor);
        }
    }

    public void ifPlatformActor(Consumer<PlatformActor> platformActorConsumer) {
        if (this instanceof PlatformActor actor) {
            platformActorConsumer.accept(actor);
        }
    }

    public void ifMember(Consumer<String> memberIdConsumer) {
        if (this instanceof OrgActor actor && actor.getPrincipalType() == MEMBER) {
            actor.getMemberId().ifPresent(memberIdConsumer);
        }
    }

    public void ifSupervisor(Consumer<String> supervisorIdConsumer) {
        if (this instanceof PlatformActor actor && actor.getPrincipalType() == SUPERVISOR) {
            actor.getSupervisorId().ifPresent(supervisorIdConsumer);
        }
    }

}
