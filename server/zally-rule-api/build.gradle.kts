dependencies {
    compile("io.swagger.core.v3:swagger-models:2.0.9")
    compile("io.swagger:swagger-models:1.5.23")
    // 2.9+ is invalid for maven publish
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}
