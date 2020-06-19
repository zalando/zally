dependencies {
    kapt(project(":zally-core"))

    compile(project(":zally-core"))
    compile("de.mpg.mpi-inf:javatools:1.1")

    testCompile(project(":zally-test"))
}
