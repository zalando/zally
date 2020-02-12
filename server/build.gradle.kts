
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.spring.io/libs-release")
    }

    extra.apply {
        // sets the jackson version that spring uses
        set("jackson.version", "2.10.1")
    }
}

plugins {
    val kotlinVersion = "1.3.21"

    kotlin("jvm").version(kotlinVersion)
    kotlin("kapt").version(kotlinVersion)
    kotlin("plugin.jpa").version(kotlinVersion)
    kotlin("plugin.noarg").version(kotlinVersion)
    kotlin("plugin.spring").version(kotlinVersion)
    kotlin("plugin.allopen").version(kotlinVersion)

    id("jacoco")
    id("org.springframework.boot") version "2.0.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("org.jlleitschuh.gradle.ktlint") version "7.2.1"
    id("org.jetbrains.dokka") version "0.10.0"
    id("maven-publish")
}

allprojects {

    val group = "de.zalando"

    val projVersion = when {
        System.getenv("JITPACK") == "true" ->
            System.getenv("VERSION")
        else -> null
    } ?: "1.0.0-dev"

    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")

    repositories {
        jcenter()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-stdlib")
    }

    kapt {
        includeCompileClasspath = false
    }

    configurations.all {
        resolutionStrategy {
            // 1.2.10 disallows jar:file: resources, hopefully fixed in 1.2.14+
            force("com.github.java-json-tools:json-schema-core:bf09fe87139ac1fde0755194b59130f3b2d63e3a")
        }
    }

    tasks.withType(KotlinCompile::class.java).all {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType(DokkaTask::class.java).all {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/dokka"
        configuration {
            reportUndocumented = false
        }
    }

    tasks.register("javadocJar", Jar::class) {
        dependsOn(tasks["dokka"])
        archiveClassifier.set("javadoc")
        from(tasks["dokka"])
    }

    tasks.register("sourcesJar", Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", tasks["javadocJar"])
        add("archives", tasks["sourcesJar"])
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = group
                artifactId = project.name
                version = if (projVersion.endsWith("-dev")) projVersion.replace("-dev", "-SNAPSHOT") else projVersion

                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
                val isSnapshot = projVersion.toString().endsWith("-SNAPSHOT")
                url = uri(if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    // defined in travis project settings or in $HOME/.gradle/gradle.properties
                    username = System.getenv("OSSRH_JIRA_USERNAME")
                    password = System.getenv("OSSRH_JIRA_PASSWORD")
                }
            }
        }
    }
}

dependencyManagement {
    dependencies {
        dependency("org.assertj:assertj-core:3.11.0")
    }
}

dependencies {
    val springBootVersion = "2.0.4.RELEASE"
    val jadlerVersion = "1.3.0"

    compile(project(":zally-core"))
    compile(project(":zally-ruleset-zalando"))
    compile(project(":zally-ruleset-zally"))
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
    compile("com.fasterxml.jackson.module:jackson-module-parameter-names:2.10.2")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.2")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.10.2")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    compile("org.yaml:snakeyaml:1.24")
    compile("org.zalando.stups:stups-spring-oauth2-server:1.0.22")
    compile("org.zalando:problem-spring-web:0.23.0")
    compile("org.zalando:twintip-spring-web:1.1.0")

    testCompile(project(":zally-test"))
    testCompile("net.jadler:jadler-core:$jadlerVersion")
    testCompile("net.jadler:jadler-jdk:$jadlerVersion")
    testCompile("net.jadler:jadler-junit:$jadlerVersion")
    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testCompile("com.jayway.jsonpath:json-path-assert:2.4.0")
    testCompile("org.mockito:mockito-core:2.23.4")
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
    archiveBaseName.set(project.name)
    archiveVersion.set(version)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "5.3.1"
}
