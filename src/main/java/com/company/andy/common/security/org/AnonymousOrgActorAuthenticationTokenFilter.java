package com.company.andy.common.security.org;

import com.company.andy.common.model.actor.PlatformActor;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.UUID;

import static com.company.andy.common.model.actor.Actor.createAnonymousActor;
import static com.company.andy.common.model.actor.ActorOrigin.fromOrgApiCall;
import static com.company.andy.common.utils.Constants.ANONYMOUS_ROLE;
import static com.company.andy.common.utils.Constants.ROLE_PREFIX;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@NullMarked
public class AnonymousOrgActorAuthenticationTokenFilter extends AnonymousAuthenticationFilter {
    private final String theKey;

    public AnonymousOrgActorAuthenticationTokenFilter() {
        String key = UUID.randomUUID().toString();
        super(key);
        this.theKey = key;
    }

    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
        PlatformActor anonymousActor = createAnonymousActor(fromOrgApiCall(request));
        return new AnonymousAuthenticationToken(this.theKey, anonymousActor, createAuthorityList(ROLE_PREFIX + ANONYMOUS_ROLE));
    }
}
