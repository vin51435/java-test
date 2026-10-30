package com.manasvia.backend.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

class ApplicationStartupListenerTest {

    @Test
    @DisplayName("onApplicationReady should execute safely and log startup information without throwing exceptions")
    void testOnApplicationReady() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.application.name", "test-backend");
        env.setProperty("server.port", "8080");
        env.setProperty("server.servlet.context-path", "");
        env.setProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        env.setActiveProfiles("dev");

        ApplicationStartupListener listener = new ApplicationStartupListener(env);

        SpringApplication app = mock(SpringApplication.class);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        ApplicationReadyEvent event = new ApplicationReadyEvent(app, new String[]{}, context, java.time.Duration.ZERO);

        assertThatNoException().isThrownBy(() -> listener.onApplicationReady(event));
    }
}
