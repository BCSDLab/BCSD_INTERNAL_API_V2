plugins {
	java
	id("org.springframework.boot") version "4.0.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.bcsdlab"
version = "0.0.1-SNAPSHOT"
description = "BCSD Internal API V2"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("software.amazon.awssdk:bom:2.53.2"))
	implementation("software.amazon.awssdk:ses")
	// software.amazon.awssdk:s3에 S3Presigner가 포함되어 있어 별도 s3-presigner 아티팩트가 없다
	// (AWS SDK가 이 버전대에서 통합함 — Maven Central에 s3-presigner 좌표 자체가 존재하지 않는다).
	implementation("software.amazon.awssdk:s3")
	implementation("org.springframework.retry:spring-retry:2.0.13")
	implementation("org.aspectj:aspectjweaver:1.9.25")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	developmentOnly("me.paulschwarz:springboot4-dotenv:5.1.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter:1.21.4")
	testImplementation("org.testcontainers:postgresql:1.21.4")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
