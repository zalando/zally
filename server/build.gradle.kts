import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

plugins {
    val kotlinVersion = "1.8.0"
    val klintVersion = "11.0.0"

    // The buildscript is also kotlin, so we apply at the root level
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    // We need to declare these here since we are configuring them for
    // subprojects from the top level.
    jacoco
    `maven-publish`
    signing
    eclipse

    id("org.jetbrains.dokka") version "1.7.20" apply false
    id("com.github.ben-manes.versions") version "0.44.0"
    id("com.diffplug.spotless") version "6.12.1"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "org.zalando"

    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")
    apply(plugin = "signing")
    apply(plugin = "eclipse")

    kapt {
        includeCompileClasspath = false
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.withType<DokkaTask>().configureEach {
        outputDirectory.set(buildDir.resolve("dokka"))
    }

    tasks.register("javadocJar", Jar::class) {
        dependsOn(tasks["dokkaJavadoc"])
        archiveClassifier.set("javadoc")
        from(tasks["dokkaJavadoc"])
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
                from(components["java"])
                pom {
                    description.set("OpenAPI linter service")
                    url.set("https://github.com/zalando/zally")
                    name.set("OpenAPI linter")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            name.set("Felix Müller")
                            email.set("felix.mueller.berlin@googlemail.com")
                        }
                        developer {
                            name.set("Mikhail Chernykh")
                            email.set("netmisch@gmail.com")
                        }
                        developer {
                            name.set("Maxim Tschumak")
                            email.set("maxim.tschumak@gmail.com")
                        }
                        developer {
                            name.set("Rui Araujo")
                            email.set("rui.araujo@zalando.de")
                        }
                        developer {
                            name.set("Tronje Krop")
                            email.set("tronje.krop@zalando.de")
                        }
                        developer {
                            name.set("Gregor Zeitlinger")
                            email.set("gregor.zeitlinger@zalando.de")
                        }
                        developer {
                            name.set("Vadim Shaigorodskiy")
                            email.set("vadim.shaigorodskiy@zalando.de")
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

    signing {
        sign(publishing.publications["mavenJava"])
    }

    configurations.all {
        resolutionStrategy {
            force("com.github.java-json-tools:json-schema-core:1.2.14")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        // We define this here so all subprojects use the same version of jackson
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
        implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.1")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
        implementation("org.yaml:snakeyaml:1.33")

        testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.1")
        testImplementation("com.jayway.jsonpath:json-path-assert:2.7.0")
        testImplementation("org.mockito:mockito-core:4.11.0")
    }

    jacoco {
        toolVersion = "0.8.8"
    }

    spotless {
        kotlin {
            target("**/*.kt")
            ktlint("0.47.1")
        }
        kotlinGradle {
            target("**/*.gradle.kts", "*.gradle.kts")
            ktlint("0.47.1")
        }
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
        systemProperty("bind-type", "true")
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    tasks.jar {
        archiveBaseName.set(project.name)
    }

    eclipse {
        project {
            natures.add("org.jetbrains.kotlin.core.kotlinNature")
        }
    }
}
