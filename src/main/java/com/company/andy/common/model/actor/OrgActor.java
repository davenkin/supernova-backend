package com.company.andy.common.model.actor;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Optional;
import java.util.Set;

import static com.company.andy.common.model.actor.ActorType.ORG_ACTOR;
import static lombok.AccessLevel.PRIVATE;

// Actor representing an organization actor with the following scenarios:
// - member of an org
// - service client acting for an org
// - org api key
// - supervisor acting for an org

@FieldNameConstants
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class OrgActor extends Actor {
    private String memberId;

    @Getter
    private String orgId;

    @Getter
    private Set<OrgRole> roles;

    public OrgActor(String id,
                    String name,
                    String memberId,
                    String orgId,
                    Set<OrgRole> roles,
                    PrincipalType principalType,
                    ActorOrigin origin) {
        super(id, name, ORG_ACTOR, principalType, origin);
        this.memberId = memberId;
        this.orgId = orgId;
        this.roles = roles;
    }

    public Optional<String> getMemberId() {
        return Optional.ofNullable(memberId);
    }

}
