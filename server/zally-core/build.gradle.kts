dependencies {
    kapt("com.google.auto.service:auto-service:1.0-rc6")

    compile(project(":zally-rule-api"))
    compile("io.swagger.parser.v3:swagger-parser:2.0.17")
    compile("io.github.config4k:config4k:0.4.1")
    compile("com.google.auto.service:auto-service:1.0-rc6")

    testCompile(project(":zally-test"))
}
