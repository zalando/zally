dependencies {
    kapt("com.google.auto.service:auto-service:1.0.1")

    api(project(":zally-rule-api"))
    api("io.swagger.parser.v3:swagger-parser:2.1.16")
    api("com.sun.mail:mailapi:1.6.2")
    api("io.github.config4k:config4k:0.5.0")
    implementation("com.google.auto.service:auto-service:1.0.1")

    testImplementation(project(":zally-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
