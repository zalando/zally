dependencies {
    kapt(project(":zally-core"))

    implementation(project(":zally-core"))
    implementation("de.mpg.mpi-inf:javatools:1.1")

    testImplementation(project(":zally-test"))
}
