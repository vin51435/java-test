package com.manasvia.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Enterprise OpenAPI 3.0 / Swagger UI configuration for the Manasvia Backend.
 * Configures interactive documentation, server environments, and JWT Bearer authorization.
 * Only instantiated when springdoc.api-docs.enabled is true (disabled in production).
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${server.port:8080}") String serverPort
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Manasvia Backend API")
                        .description("""
                                **Ancient wisdom for your modern mind.**  
                                High-concurrency AI-powered spiritual and mental wellness companion backend.  
                                Built on **Java 21 Virtual Threads (Loom)**, **Spring Boot 4.x**, **PostgreSQL 16 + pgvector**, and **Redis 7.2**.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Manasvia Engineering Team")
                                .email("engineering@manasvia.com")
                                .url("https://manasvia.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://manasvia.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.manasvia.com")
                                .description("Production Cloud Run / VPS Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your stateless JWT access token issued by `/api/v1/auth/guest` or Google OAuth.")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
