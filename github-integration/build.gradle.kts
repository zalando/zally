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

    compile("org.kohsuke:github-api:1.86")
    compile("io.swagger:swagger-parser:1.0.28")
    compile("com.github.java-json-tools:json-schema-validator:2.2.8")
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")
    compile("org.springframework.boot:spring-boot-starter-undertow")
    compile("org.springframework.boot:spring-boot-starter-actuator")
    compile("org.springframework.boot:spring-boot-starter-data-jpa")
    compile("org.thymeleaf:thymeleaf-spring4")
    compile("org.flywaydb:flyway-core:4.1.2")
    compile("org.postgresql:postgresql:42.1.1")
    compile("org.hibernate:hibernate-core:$hibernateVersion")
    compile("org.hibernate:hibernate-entitymanager:$hibernateVersion")
    compile("org.jadira.usertype:usertype.core:6.0.1.GA")
    compile("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    compile("org.zalando.stups:stups-spring-oauth2-server:1.0.18")
    compile("org.zalando:problem-spring-web:0.19.0")
    compile("org.zalando:twintip-spring-web:1.1.0")
    compile("org.zalando.zmon:zmon-actuator:0.9.7")
    compile("io.dropwizard.metrics:metrics-core:3.2.2")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.0")
    compile("org.jetbrains.kotlin:kotlin-reflect")
    compile("org.jetbrains.kotlin:kotlin-stdlib")
    compile("io.github.openfeign:feign-core:9.5.1")
    compile("io.github.openfeign:feign-jackson:9.5.1")
    compile("org.zalando.stups.build:tokens-k8s-spring-boot-starter:0.1.1") {
        exclude("org.slf4j", "slf4j-simple")
    }
    compile("uk.co.datumedge:hamcrest-json:0.2")
    compile("org.postgresql:postgresql:42.1.4")
    compile("io.github.microutils:kotlin-logging:1.6.22")
    compile("javax.xml.bind:jaxb-api:2.3.0")
    compile("com.sun.xml.bind:jaxb-core:2.3.0")
    compile("com.sun.xml.bind:jaxb-impl:2.3.0")
    compile("javax.activation:activation:1.1.1")

    testCompile("ru.yandex.qatools.embed:postgresql-embedded:2.10") {
        exclude("de.flapdoodle.embed", "de.flapdoodle.embed.process")
    }
    testCompile("de.flapdoodle.embed:de.flapdoodle.embed.process:2.0.5")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("org.mockito:mockito-core:1.10.19")
    testCompile("net.jadler:jadler-core:1.3.0")
    testCompile("net.jadler:jadler-jdk:1.3.0")
    testCompile("net.jadler:jadler-junit:1.3.0")
    testCompile("com.nhaarman:mockito-kotlin:1.6.0")
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
