dependencies {
    kapt(project(":zally-core"))

    implementation(project(":zally-core"))

    testImplementation(project(":zally-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
