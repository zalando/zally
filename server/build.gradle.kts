import de.undercouch.gradle.tasks.download.Download
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
    id("org.springframework.boot") version "2.0.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("de.undercouch.download") version "3.4.3"
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

    tasks.register("sourcesJar", Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", tasks["sourcesJar"])
    }
}

dependencies {
    val springBootVersion = "2.0.4.RELEASE"
    val jadlerVersion = "1.3.0"

    compile(project("zally-rule-api"))
    compile("org.jetbrains.kotlin:kotlin-stdlib")
    compile("io.swagger.parser.v3:swagger-parser:2.0.2")
    compile("com.github.java-json-tools:json-schema-validator:2.2.10")
    compile("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-undertow:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    compile("org.flywaydb:flyway-core:5.1.4")
    compile("org.hsqldb:hsqldb:2.4.1")
    compile("org.postgresql:postgresql:42.2.4")
    compile("org.hibernate:hibernate-core:5.3.5.Final")
    compile("org.jadira.usertype:usertype.core:7.0.0.CR1") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    compile("com.fasterxml.jackson.module:jackson-module-parameter-names")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9+")
    compile("org.zalando.stups:stups-spring-oauth2-server:1.0.22")
    compile("org.zalando:problem-spring-web:0.23.0")
    compile("org.zalando:twintip-spring-web:1.1.0")
    compile("io.github.config4k:config4k:0.4.1")

    compile("de.mpg.mpi-inf:javatools:1.1")

    testCompile(project("zally-rule-api"))

    testCompile("net.jadler:jadler-core:$jadlerVersion")
    testCompile("net.jadler:jadler-jdk:$jadlerVersion")
    testCompile("net.jadler:jadler-junit:$jadlerVersion")
    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testCompile("org.assertj:assertj-core:3.11.0")
    testCompile("com.jayway.jsonpath:json-path-assert:2.4.0")
    testCompile("org.mockito:mockito-core:2.23.4")
}

jacoco {
    toolVersion = "0.8.2"
}

tasks.register("downloadJsonSchema", Download::class) {
    src("http://json-schema.org/draft-04/schema")
    dest("$rootDir/src/main/resources/schemas/json-schema.json")
    onlyIfModified(true)
}

tasks.register("downloadSwaggerSchema", Download::class) {
    src("http://swagger.io/v2/schema.json")
    dest("$rootDir/src/main/resources/schemas/swagger-schema.json")
    onlyIfModified(true)
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
    archiveBaseName.set("zally")
    archiveVersion.set("1.0.0")
}

tasks.processResources {
    dependsOn("downloadJsonSchema")
    dependsOn("downloadSwaggerSchema")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "5.3.1"
}
