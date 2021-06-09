dependencies {
    kapt("com.google.auto.service:auto-service:1.0-rc6")

    api(project(":zally-rule-api"))
    api("io.swagger.parser.v3:swagger-parser:2.0.26")
    api("io.github.config4k:config4k:0.4.1")
    implementation("com.google.auto.service:auto-service:1.0-rc6")

    testImplementation(project(":zally-test"))
}
