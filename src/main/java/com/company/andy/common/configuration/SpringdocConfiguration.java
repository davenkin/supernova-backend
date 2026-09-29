package com.company.andy.common.configuration;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SpringdocConfiguration {

    @Bean
    public GlobalOpenApiCustomizer globalErrorResponseCustomizer() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> {
                pathItem.readOperations().forEach(operation -> {
                    Content errorContent = new Content().addMediaType(
                            "application/json",
                            new MediaType().schema(new ObjectSchema().$ref("#/components/schemas/QApiErrorResponse"))
                    );

                    ApiResponses responses = operation.getResponses();
                    responses.putIfAbsent("400", new ApiResponse().description("Bad request").content(errorContent));
                    responses.putIfAbsent("401", new ApiResponse().description("Authentication failed, same error structure as 400"));
                    responses.putIfAbsent("403", new ApiResponse().description("Access denied, same error structure as 400"));
                    responses.putIfAbsent("404", new ApiResponse().description("Not found, same error structure as 400"));
                    responses.putIfAbsent("409", new ApiResponse().description("Business rule check failed, same error structure as 400"));
                    responses.putIfAbsent("500", new ApiResponse().description("Internal server error, same error structure as 400"));
                });
            });
        };
    }
}
