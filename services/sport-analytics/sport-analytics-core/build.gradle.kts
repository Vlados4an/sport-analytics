dependencies {
    api(project(":libraries:di"))
    api(project(":libraries:noarg"))
    api(project(":libraries:database-utils"))
    api(project(":libraries:jackson-config"))
    api(project(":services:sport-analytics:sport-analytics-api"))
    api(project(":services:sport-analytics:sport-analytics-jooq"))

    implementation(libs.clickhouse.http.client)
    implementation(libs.redisson)
    implementation(libs.okhttp)
    implementation(libs.jbcrypt)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.slf4j.api)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
}
