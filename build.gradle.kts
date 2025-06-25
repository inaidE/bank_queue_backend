plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "9.16.0"
}

group = "com.bankqueue"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// ===== APPLICATION =====
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	implementation("com.vladmihalcea:hibernate-types-60:2.20.0")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")


	// ===== TEST =====
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		// Spring Boot Starter Test уже подтягивает JUnit Jupiter 5.9.x
		exclude(module = "junit")             // исключаем старый JUnit4
		exclude(group = "org.junit.vintage")  // исключаем винтажный движок
	}
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation(kotlin("test"))
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

}

tasks.withType<Test> {
	useJUnitPlatform()  // обязательно для JUnit 5
}
