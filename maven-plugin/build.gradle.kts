plugins {
    val kotlinVersion = "1.4.32"
    val klintVersion = "9.2.1"

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    id("de.benediktritter.maven-plugin-development") version "0.3.1"
    `maven-publish`

    id("org.jlleitschuh.gradle.ktlint") version klintVersion
    id("org.jlleitschuh.gradle.ktlint-idea") version klintVersion
}

group = "org.zalando"
version = "1.0.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.zalando:zally-core:2.0.0")
    // for explicit overlapping transitive 1.3.61 dependency from jackson
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")

    implementation("org.apache.maven:maven-plugin-api:3.6.3")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.mockito:mockito-inline:3.11.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                description.set("A Maven plugin for validating OpenAPI and Swagger specs with Zally")
                url.set("https://github.com/zalando/zally")
                name.set("Zally Maven plugin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/zalando/zally.git")
                    developerConnection.set("scm:git:ssh://github.com:zalando/zally.git")
                    url.set("https://github.com/zalando/zally/tree/master")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                // defined in travis project settings or in $HOME/.gradle/gradle.properties
                username = System.getenv("OSSRH_JIRA_USERNAME")
                password = System.getenv("OSSRH_JIRA_PASSWORD")
            }
        }
    }
}
