dependencies {
    kapt(project(":zally-core"))

    implementation(project(":zally-core"))
    implementation("de.mpg.mpi-inf:javatools:2017-06-28")

    testImplementation(project(":zally-test"))
}
