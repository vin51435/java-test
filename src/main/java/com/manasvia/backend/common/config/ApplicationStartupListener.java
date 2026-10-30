package com.manasvia.backend.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Development-only startup listener that prints local network URLs,
 * Swagger UI, and OpenAPI doc links once the Spring Application context is fully ready.
 * Completely disabled in production and test profiles to prevent blocking calls and log pollution.
 */
@Component
@Profile("dev")
public class ApplicationStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupListener.class);

    private final Environment env;

    public ApplicationStartupListener(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {

        // Determines whether URLs should be http:// or https://
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::hasText)
                .orElse("");
        String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String[] activeProfiles = env.getActiveProfiles();
        String profiles = activeProfiles.length == 0 ? "default" : String.join(", ", activeProfiles);

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host network address could not be determined, using `localhost` as fallback");
        }

        log.info("""
                
                --------------------------------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal:       {}://localhost:{}{}
                \tNetwork:     {}://{}:{}{}
                \tSwagger UI:  {}://{}:{}{}{}
                \tOpenAPI Doc: {}://{}:{}{}/v3/api-docs
                \tProfile(s):  [{}]
                --------------------------------------------------------------------------------""",
                env.getProperty("spring.application.name", "manasvia-backend"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath, swaggerPath,
                protocol, hostAddress, serverPort, contextPath,
                profiles
        );
    }
}
