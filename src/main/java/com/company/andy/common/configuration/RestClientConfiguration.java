package com.company.andy.common.configuration;

import com.company.andy.common.configuration.profile.DisableForIT;
import com.company.andy.common.security.JwtRelayRestClientRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import static com.company.andy.common.utils.Constants.*;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

// Disable for integration tests,
// as we don't want to make real api calls to external systems in integration tests

@DisableForIT
@Configuration(proxyBeanMethods = false)
public class RestClientConfiguration {

    // RestClient that relays current actor's JWT token for calling external APIs
    @Bean(JWT_RELAY_REST_CLIENT)
    public RestClient jwtRelayRestClient(RestClient.Builder builder, JwtRelayRestClientRequestInterceptor jwtRelayRestClientRequestInterceptor) {
        return builder.defaultHeader(ACCEPT, APPLICATION_JSON_VALUE).requestInterceptor(jwtRelayRestClientRequestInterceptor).build();
    }

    // RestClient that represents the application itself with JWT token being obtained automatically by Spring using Oauth2 client_credentials grant type
    @Bean(SERVICE_CLIENT_REST_CLIENT)
    public RestClient serviceClientRestClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(_ -> KEYCLOAK_SERVICE_ACCOUNT);
        return builder.defaultHeader(ACCEPT, APPLICATION_JSON_VALUE).requestInterceptor(interceptor).build();
    }

    // This is required by "SERVICE_CLIENT_REST_CLIENT" bean to make Oauth2 client_credentials grant type work properly
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
}
