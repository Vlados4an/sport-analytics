dependencies {
    api(project(":libraries:di"))
    api(project(":libraries:environment"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)
}
