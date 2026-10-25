plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.manasvia"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")

	// JJWT (Java JWT) for stateless authentication
	val jjwtVersion = "0.12.6"
	implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// =============================================================================
// Enterprise Convenience Tasks (Equivalent to Node.js npm scripts)
// =============================================================================

// Default bootRun sets dev profile unless overridden
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
	systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active") ?: "dev")
}

// ./gradlew dev -> Starts backend in dev mode
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("dev") {
	group = "application"
	description = "Runs Manasvia Backend in DEV mode (Virtual Threads, SQL debug, Local Docker)"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.manasvia.backend.BackendApplication")
	systemProperty("spring.profiles.active", "dev")
}

// ./gradlew prod -> Starts backend in prod mode
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("prod") {
	group = "application"
	description = "Runs Manasvia Backend with PROD profile settings"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.manasvia.backend.BackendApplication")
	systemProperty("spring.profiles.active", "prod")
}

// ./gradlew dockerUp -> Starts postgres (pgvector) and redis
tasks.register<Exec>("dockerUp") {
	group = "docker"
	description = "Starts PostgreSQL (pgvector) and Redis containers"
	workingDir = projectDir
	commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
		listOf("cmd", "/c", "docker compose up -d")
	} else {
		listOf("docker", "compose", "up", "-d")
	}
}

// ./gradlew dockerDown -> Stops containers
tasks.register<Exec>("dockerDown") {
	group = "docker"
	description = "Stops PostgreSQL and Redis containers"
	workingDir = projectDir
	commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
		listOf("cmd", "/c", "docker compose down")
	} else {
		listOf("docker", "compose", "down")
	}
}

// ./gradlew installHooks -> Configures Husky-equivalent Git hooks & commitlint
tasks.register<Exec>("installHooks") {
	group = "git"
	description = "Configures Git to use version-controlled .githooks and commit template"
	workingDir = projectDir
	commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
		listOf("cmd", "/c", "git config core.hooksPath .githooks && git config commit.template .gitmessage")
	} else {
		listOf("sh", "-c", "git config core.hooksPath .githooks && git config commit.template .gitmessage")
	}
}
