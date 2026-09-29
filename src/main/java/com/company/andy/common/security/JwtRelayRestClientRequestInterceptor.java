package com.company.andy.common.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@NullMarked
public class JwtRelayRestClientRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ActorAuthenticationToken token) {
            request.getHeaders().setBearerAuth(token.getJwt().getTokenValue());
        } else if (authentication instanceof BearerTokenAuthenticationToken bearerToken) {
            request.getHeaders().setBearerAuth(bearerToken.getToken());
        } else if (authentication instanceof JwtAuthenticationToken token) {
            request.getHeaders().setBearerAuth(token.getToken().getTokenValue());
        }
        return execution.execute(request, body);
    }
}