package com.manasvia.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Architectural Boundary Verification and Living Documentation Generator.
 * Powered by Spring Modulith.
 */
class ArchitectureTests {

    private final ApplicationModules modules = ApplicationModules.of(BackendApplication.class);

    @Test
    void printModuleOverview() {
        modules.forEach(System.out::println);
    }

    @Test
    void verifyModularArchitecture() {
        modules.verify();
    }

    @Test
    void generateDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
