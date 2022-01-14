dependencies {
    implementation("io.swagger.core.v3:swagger-models:2.1.1")
    implementation("io.swagger:swagger-models:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.11.0")
}
