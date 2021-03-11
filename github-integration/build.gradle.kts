import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.spring.io/libs-release")
    }
}

plugins {
    val kotlinVersion = "1.3.21"

    kotlin("jvm").version(kotlinVersion)
    kotlin("plugin.jpa").version(kotlinVersion)
    kotlin("plugin.noarg").version(kotlinVersion)
    kotlin("plugin.spring").version(kotlinVersion)
    kotlin("plugin.allopen").version(kotlinVersion)

    id("jacoco")
    id("org.springframework.boot") version "1.5.19.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
}

allprojects {

    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        jcenter()
        mavenCentral()
    }

    tasks.withType(KotlinCompile::class.java).all {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {

    val hibernateVersion = "5.2.18.Final"
    val jacksonVersion = "2.9.8"

    implementation("org.kohsuke:github-api:1.86")
    implementation("io.swagger:swagger-parser:1.0.28")
    implementation("com.github.java-json-tools:json-schema-validator:2.2.8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.thymeleaf:thymeleaf-spring4")
    implementation("org.flywaydb:flyway-core:4.1.2")
    implementation("org.postgresql:postgresql:42.1.1")
    implementation("org.hibernate:hibernate-core:$hibernateVersion")
    implementation("org.hibernate:hibernate-entitymanager:$hibernateVersion")
    implementation("org.jadira.usertype:usertype.core:6.0.1.GA")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.zalando.stups:stups-spring-oauth2-server:1.0.18")
    implementation("org.zalando:problem-spring-web:0.19.0")
    implementation("org.zalando:twintip-spring-web:1.1.0")
    implementation("org.zalando.zmon:zmon-actuator:0.9.7")
    implementation("io.dropwizard.metrics:metrics-core:3.2.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.github.openfeign:feign-core:9.5.1")
    implementation("io.github.openfeign:feign-jackson:9.5.1")
    implementation("org.zalando.stups.build:tokens-k8s-spring-boot-starter:0.1.1") {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation("uk.co.datumedge:hamcrest-json:0.2")
    implementation("org.postgresql:postgresql:42.1.4")
    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.0")
    implementation("javax.activation:activation:1.1.1")

    testImplementation("ru.yandex.qatools.embed:postgresql-embedded:2.10") {
        exclude("de.flapdoodle.embed", "de.flapdoodle.embed.process")
    }
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.process:2.0.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:1.10.19")
    testImplementation("net.jadler:jadler-core:1.3.0")
    testImplementation("net.jadler:jadler-jdk:1.3.0")
    testImplementation("net.jadler:jadler-junit:1.3.0")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
}

jacoco {
    toolVersion = "0.8.2"
}

tasks.bootRun {
    jvmArgs = listOf("-Dspring.profiles.active=dev")
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

tasks.jar {
    archiveVersion.set("1.0.0")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "5.3.1"
}
