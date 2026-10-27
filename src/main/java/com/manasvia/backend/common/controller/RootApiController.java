package com.manasvia.backend.common.controller;

import com.manasvia.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Root health and status controller for the Manasvia Backend.
 */
@RestController
@Tag(name = "00. System", description = "Root health, metadata, and runtime diagnostics")
public class RootApiController {

    @Value("${spring.application.name:backend}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @GetMapping("/")
    @Operation(summary = "Root server diagnostic probe", description = "Returns system uptime status, active profile, and architecture metadata.")
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
