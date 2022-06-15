dependencies {
    kapt("com.google.auto.service:auto-service:1.0.1")

    api(project(":zally-rule-api"))
    api("io.swagger.parser.v3:swagger-parser:2.0.32")
    api("io.github.config4k:config4k:0.4.2")
    implementation("com.google.auto.service:auto-service:1.0.1")

    testImplementation(project(":zally-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
