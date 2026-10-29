package com.manasvia.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Optional;

@SpringBootApplication
@EntityScan(basePackages = {
		"com.manasvia.backend",
		"org.springframework.modulith.events.jpa"
})
public class BackendApplication {

	private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

	private final Environment env;

	public BackendApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
		String serverPort = env.getProperty("server.port", "8080");
		String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
				.filter(StringUtils::hasText)
				.orElse("");
		String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
		String[] activeProfiles = env.getActiveProfiles();
		String profiles = activeProfiles.length == 0 ? "default" : String.join(", ", activeProfiles);

		log.info("""
				
				--------------------------------------------------------------------------------
				\tApplication '{}' is running! Access URLs:
				\tLocal API:   {}://localhost:{}{}
				\tSwagger UI:  {}://localhost:{}{}{}
				\tOpenAPI Doc: {}://localhost:{}{}/v3/api-docs
				\tProfile(s):  [{}]
				--------------------------------------------------------------------------------""",
				env.getProperty("spring.application.name", "manasvia-backend"),
				protocol, serverPort, contextPath,
				protocol, serverPort, contextPath, swaggerPath,
				protocol, serverPort, contextPath,
				profiles
		);
	}
}
