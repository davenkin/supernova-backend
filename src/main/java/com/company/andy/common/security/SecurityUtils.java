package com.company.andy.common.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.company.andy.common.utils.Constants.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SecurityUtils {
    public static Set<String> getJwtRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(JWT_CLAIM_REALM_ACCESS);
        if (realmAccess == null) {
            return Set.of();
        }

        Object roles = realmAccess.get(JWT_CLAIM_REALM_ACCESS_ROLES);

        if (roles == null) {
            return Set.of();
        }

        if (roles instanceof Collection<?> theRoles) {
            return theRoles.stream().map(Object::toString).collect(toSet());
        }
        return Set.of();
    }

    public static String getJwtUserName(Jwt jwt) {
        return jwt.getClaimAsString(JWT_CLAIM_PREFERRED_USERNAME);
    }

    public static String getJwtPrincipalType(Jwt jwt) {
        String principalType = jwt.getClaimAsString(JWT_CLAIM_PRINCIPAL_TYPE);
        if (isBlank(principalType)) {
            throw new InvalidBearerTokenException("Cannot obtain principal_type from JWT.");
        }
        return principalType;
    }

    public static String getJwtClient(Jwt jwt) {
        String clientId = jwt.getClaimAsString(JWT_CLAIM_AZP);
        if (isBlank(clientId)) {
            throw new InvalidBearerTokenException("Cannot obtain clientId(azp) from JWT.");
        }
        return clientId;
    }

    public static String getJwtOrgId(Jwt jwt) {
        String orgId = jwt.getClaimAsString(JWT_CLAIM_ORG_ID);
        if (isBlank(orgId)) {
            throw new InvalidBearerTokenException("Cannot obtain an org_id from JWT.");
        }
        return orgId;
    }

    public static String getJwtMemberId(Jwt jwt) {
        String memberId = jwt.getClaimAsString(JWT_CLAIM_MEMBER_ID);
        if (isBlank(memberId)) {
            throw new InvalidBearerTokenException("Cannot obtain member_id from JWT.");
        }
        return memberId;
    }

    public static String getJwtSupervisorId(Jwt jwt) {
        String supervisorId = jwt.getClaimAsString(JWT_CLAIM_SUPERVISOR_ID);
        if (isBlank(supervisorId)) {
            throw new InvalidBearerTokenException("Cannot obtain supervisor_id from JWT.");
        }
        return supervisorId;
    }

}
