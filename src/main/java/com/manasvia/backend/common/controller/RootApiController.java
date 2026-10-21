package com.manasvia.backend.common.controller;

import com.manasvia.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Root health and status controller for the Manasvia Backend.
 */
@RestController
public class RootApiController {

    @Value("${spring.application.name:manasvia-backend}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @GetMapping("/")
    public ApiResponse<Map<String, Object>> rootStatus() {
        return ApiResponse.ok(
                "Manasvia Backend API is online",
                Map.of(
                        "app", appName,
                        "profile", activeProfile,
                        "status", "UP",
                        "runtime", "Java 21/25 (Virtual Threads Enabled)",
                        "architecture", "Event-Driven Modular Monolith",
                        "timestamp", Instant.now().toString()
                )
        );
    }
}
