// Version set to empty to make artifact name in line with the name defined in Dockerfile
version = ""

buildscript {
    extra.apply {
        // sets the jackson version that spring uses
        set("jackson.version", "2.12.2")
    }
}

plugins {
    val kotlinVersion = "1.4.32"

    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion

    id("org.springframework.boot") version "2.1.15.RELEASE"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    val jadlerVersion = "1.3.0"

    implementation(project(":zally-core"))
    implementation(project(":zally-ruleset-zalando"))
    implementation(project(":zally-ruleset-zally"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    implementation("org.flywaydb:flyway-core")
    implementation("org.hsqldb:hsqldb:2.4.1")
    implementation("org.postgresql:postgresql:42.2.4")
    implementation("org.hibernate:hibernate-core")
    implementation("org.jadira.usertype:usertype.core:7.0.0.CR1") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    implementation("org.zalando.stups:stups-spring-oauth2-server:1.0.22")
    implementation("org.zalando:problem-spring-web:0.23.0")
    implementation("org.zalando:twintip-spring-web:1.1.0")

    testImplementation(project(":zally-test"))
    testImplementation("net.jadler:jadler-core:$jadlerVersion")
    testImplementation("net.jadler:jadler-jdk:$jadlerVersion")
    testImplementation("net.jadler:jadler-junit:$jadlerVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.jayway.jsonpath:json-path-assert:2.4.0")
    testImplementation("org.mockito:mockito-core:2.23.4")
}

tasks.bootRun {
    jvmArgs = listOf("-Dspring.profiles.active=dev")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        false
    }
}
