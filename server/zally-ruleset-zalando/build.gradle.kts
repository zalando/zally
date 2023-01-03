import org.zalando.zally.task.MediaTypesConfigurationTask

dependencies {
    kapt(project(":zally-core"))

    implementation(project(":zally-core"))
    implementation("de.mpg.mpi-inf:javatools:2017-06-28")

    testImplementation(project(":zally-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<MediaTypesConfigurationTask>("generate-media-types-config") {
    mediaTypes.set(
        listOf(
            "application",
            "audio",
            "font",
            "image",
            "message",
            "model",
            "multipart",
            "text",
            "video"
        )
    )
    outputFile.set(file("${project.projectDir}/src/main/resources/media-types.json"))
}
