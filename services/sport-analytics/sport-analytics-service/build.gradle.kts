plugins {
    application
    alias(libs.plugins.shadow.jar)
}

application {
    mainClass.set("com.sportanalytics.service.SportAnalyticsProductionApplicationKt")
}

dependencies {
    implementation(project(":libraries:di"))
    implementation(project(":libraries:environment"))
    implementation(project(":libraries:runtime"))
    implementation(project(":libraries:jackson-config"))
    implementation(project(":libraries:database-utils"))
    implementation(project(":services:sport-analytics:sport-analytics-api"))
    implementation(project(":services:sport-analytics:sport-analytics-core"))
    implementation(project(":services:sport-analytics:sport-analytics-migration"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.kodein.di)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)
    implementation(libs.jbcrypt)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.server.test.host)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    isZip64 = true
}
