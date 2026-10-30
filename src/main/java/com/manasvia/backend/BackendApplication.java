package com.manasvia.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Main application bootstrap for the Manasvia backend platform.
 * Adheres strictly to the Single Responsibility Principle (SRP).
 */
@SpringBootApplication
@EntityScan(basePackages = {
		"com.manasvia.backend",
		"org.springframework.modulith.events.jpa"
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
