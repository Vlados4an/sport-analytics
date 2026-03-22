plugins {
    application
}

application {
    mainClass.set("com.sportanalytics.tools.jooq.JooqCodegenRunnerKt")
}

dependencies {
    implementation(libs.jooq)
    implementation(libs.jooq.meta)
    implementation(libs.jooq.codegen)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.hikaricp)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
}
