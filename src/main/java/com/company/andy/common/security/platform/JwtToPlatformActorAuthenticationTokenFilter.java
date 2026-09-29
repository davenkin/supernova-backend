package com.company.andy.common.security.platform;

import static java.util.Objects.requireNonNull;

import static com.company.andy.common.model.actor.ActorOrigin.fromPlatformApiCall;
import static com.company.andy.common.model.actor.PrincipalType.PLATFORM_SERVICE_CLIENT;
import static com.company.andy.common.model.actor.PrincipalType.SUPERVISOR;
import static com.company.andy.common.security.SecurityUtils.getJwtClient;
import static com.company.andy.common.security.SecurityUtils.getJwtPrincipalType;
import static com.company.andy.common.security.SecurityUtils.getJwtSupervisorId;
import static com.company.andy.common.security.SecurityUtils.getJwtUserName;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import com.company.andy.common.configuration.property.CommonProperties;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.common.security.ActorAuthenticationToken;
import com.company.andy.common.tracing.ActorMdcSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

// Convert JWT into PlatformActor
// Controllers can use "@AuthenticationPrincipal @NotNull PlatformActor actor" to obtain the current actor

@Slf4j
public class JwtToPlatformActorAuthenticationTokenFilter extends OncePerRequestFilter {
  private final AuthenticationEntryPoint authenticationEntryPoint;
  private final CommonProperties commonProperties;

  public JwtToPlatformActorAuthenticationTokenFilter(AuthenticationEntryPoint authenticationEntryPoint, CommonProperties commonProperties) {
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.commonProperties = commonProperties;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    boolean mdcPopulated = false;
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
          Jwt jwt = jwtAuthenticationToken.getToken();
          ActorAuthenticationToken authenticationToken = createActorAuthenticationToken(request, jwt);
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          ActorMdcSupport.addMdc(authenticationToken.getActor());
          mdcPopulated = true;
        }
      }
      filterChain.doFilter(request, response);
    } catch (AuthenticationException ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, ex);
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, new AuthenticationServiceException(ex.getMessage(), ex));
    } finally {
      if (mdcPopulated) {
        ActorMdcSupport.clearMdc();
      }
    }
  }

  private ActorAuthenticationToken createActorAuthenticationToken(HttpServletRequest request, Jwt jwt) {
    if (!Objects.equals(requireNonNull(jwt.getIssuer()).toString(), commonProperties.platformJwtIssuer())) {
      throw new InvalidBearerTokenException(
          "Invalid JWT issuer: " + jwt.getIssuer() + ", platform api expects issuer: " + commonProperties.platformJwtIssuer());
    }

    String principalType = getJwtPrincipalType(jwt);
    if (Objects.equals(principalType, SUPERVISOR.name())) {
      return new ActorAuthenticationToken(Actor.createSupervisorActor(
          getJwtSupervisorId(jwt),
          getJwtUserName(jwt),
          Set.of(),
          fromPlatformApiCall(request)
      ), Set.of(), jwt);
    }

    if (Objects.equals(principalType, PLATFORM_SERVICE_CLIENT.name())) {
      return new ActorAuthenticationToken(Actor.createPlatformServiceClientActor(
          getJwtClient(jwt),
          fromPlatformApiCall(request)
      ), Set.of(), jwt);
    }

    throw new InvalidBearerTokenException(
        "Invalid JWT principal type: " + principalType + ", platform api expects principal type: " + SUPERVISOR.name() + " or " +
        PLATFORM_SERVICE_CLIENT.name());
  }
}
