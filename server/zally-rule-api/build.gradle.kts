dependencies {
    implementation("io.swagger.core.v3:swagger-models:2.2.0")
    implementation("io.swagger:swagger-models:1.6.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.22.0")
}
