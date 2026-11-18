// Version set to empty to make artifact name in line with the name defined in Dockerfile
version = ""

plugins {
    val kotlinVersion = "2.2.21"

    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion

    id("org.springframework.boot") version "3.5.16"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    constraints {
        // swagger-parser-v2-converter (via zally-core) brings io.swagger:swagger-parser 1.0.64, whose YAML constructor calls
        // SafeConstructor() and fails on snakeyaml 2, which Spring Boot 3 manages. 1.0.70+ passes LoaderOptions.
        implementation("io.swagger:swagger-parser:1.0.76") {
            because("1.0.64 is incompatible with snakeyaml 2")
        }
        implementation("io.swagger.parser.v3:swagger-parser-v3:2.1.22") {
            because("2.1.12 calls SafeConstructor() as well and logs 'Error snake-parsing yaml content' for every YAML definition")
        }
    }

    val jadlerVersion = "1.3.1"

    implementation(project(":zally-core"))
    implementation(project(":zally-ruleset-zalando"))
    implementation(project(":zally-ruleset-zally"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-hsqldb")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.hsqldb:hsqldb")
    implementation("org.postgresql:postgresql")

    testImplementation(project(":zally-test"))
    testImplementation("net.jadler:jadler-core:$jadlerVersion")
    testImplementation("net.jadler:jadler-jdk:$jadlerVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.jayway.jsonpath:json-path-assert:2.7.0")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

tasks.bootRun {
    jvmArgs = listOf("-Dspring.profiles.active=dev")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        false
    }
}
