dependencies {
    kapt("com.google.auto.service:auto-service:1.0-rc6")

    implementation(project(":zally-rule-api"))
    implementation("io.swagger.parser.v3:swagger-parser:2.0.17")
    implementation("io.github.config4k:config4k:0.4.1")
    implementation("com.google.auto.service:auto-service:1.0-rc6")

    testImplementation(project(":zally-test"))
}
