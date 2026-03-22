package com.sportanalytics.tools.jooq

import org.flywaydb.core.Flyway
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import java.sql.DriverManager

/**
 * jOOQ code generator — same pattern as mono-repo's JooqGeneratorService.
 *
 * Two modes:
 * 1. --testcontainers: spin up PG via Testcontainers (Linux/WSL)
 * 2. Default: connect to running PG from docker-compose (Windows)
 *
 * Usage:
 *   docker compose up -d postgres     # ensure PG is running
 *   ./gradlew :tools:jooq-generator:run
 *
 * Or in mono style:
 *   ./cli--run-code-generators-jooq.sh
 */
private val log = LoggerFactory.getLogger("com.sportanalytics.tools.jooq.JooqCodegenRunner")

fun main(args: Array<String>) {
    val dbHost = System.getenv("JOOQ_DB_HOST") ?: "localhost"
    val dbPort = System.getenv("JOOQ_DB_PORT") ?: "5432"
    val dbName = System.getenv("JOOQ_DB_NAME") ?: "sport_analytics"
    val dbUser = System.getenv("JOOQ_DB_USER") ?: "sa_user"
    val dbPass = System.getenv("JOOQ_DB_PASSWORD") ?: "sa_password"

    val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

    log.info("Starting jOOQ code generation against $jdbcUrl")

    // 1. Ensure DB exists and create if needed
    try {
        DriverManager.getConnection(jdbcUrl, dbUser, dbPass).use { conn ->
            log.info("Connected to database '$dbName'")
        }
    } catch (e: Exception) {
        log.info("Database '$dbName' not accessible, attempting to create...")
        val rootUrl = "jdbc:postgresql://$dbHost:$dbPort/postgres"
        DriverManager.getConnection(rootUrl, dbUser, dbPass).use { conn ->
            conn.createStatement().execute("CREATE DATABASE $dbName")
            log.info("Database '$dbName' created")
        }
    }

    // 2. Run Flyway migrations
    val cwd = System.getProperty("user.dir")
    // Gradle runs from subproject dir — find the root by looking for settings.gradle.kts
    val projectRoot = generateSequence(java.io.File(cwd)) { it.parentFile }
        .firstOrNull { java.io.File(it, "settings.gradle.kts").exists() }
        ?.absolutePath ?: cwd
    val migrationsPath = "filesystem:$projectRoot/services/sport-analytics/sport-analytics-migration/src/main/resources/db/migration"

    log.info("Running Flyway migrations from: $migrationsPath")

    val migrationResult = Flyway.configure()
        .dataSource(jdbcUrl, dbUser, dbPass)
        .locations(migrationsPath)
        .load()
        .migrate()

    log.info("Flyway: migrations completed (result=$migrationResult)")

    // 3. Resolve output directory
    val outputDir = "$projectRoot/services/sport-analytics/sport-analytics-jooq/src/main/java"
    log.info("Output directory: $outputDir")

    // 4. Run jOOQ GenerationTool (same config as mono-repo's JooqGeneratorService)
    GenerationTool.generate(
        Configuration()
            .withJdbc(
                Jdbc()
                    .withDriver("org.postgresql.Driver")
                    .withUrl(jdbcUrl)
                    .withUser(dbUser)
                    .withPassword(dbPass),
            )
            .withGenerator(
                Generator()
                    .withName("org.jooq.codegen.JavaGenerator")
                    .withDatabase(
                        Database()
                            .withName("org.jooq.meta.postgres.PostgresDatabase")
                            .withInputSchema("public")
                            .withIncludes(".*")
                            .withExcludes("flyway_schema_history")
                            .withForcedTypes(
                                ForcedType()
                                    .withName("INSTANT")
                                    .withIncludeTypes("""(?i:TIMESTAMP\ WITH\ TIME\ ZONE|TIMESTAMP)"""),
                            )
                            .apply {
                                isForceIntegerTypesOnZeroScaleDecimals = false
                            },
                    )
                    .withGenerate(
                        Generate()
                            .withComments(false)
                            .withJavadoc(false)
                            .withGlobalCatalogReferences(false)
                            .withGlobalSchemaReferences(false)
                            .withGlobalTableReferences(false)
                            .withGlobalSequenceReferences(false)
                            .withGlobalUDTReferences(false)
                            .withGlobalRoutineReferences(true)
                            .withGlobalQueueReferences(false)
                            .withGlobalLinkReferences(false)
                            .withGlobalKeyReferences(true)
                            .withGlobalIndexReferences(false)
                            .withDeprecated(false)
                            .withFluentSetters(true)
                            .withIndexes(true),
                    )
                    .withTarget(
                        Target()
                            .withPackageName("com.sportanalytics.jooq")
                            .withDirectory(outputDir),
                    ),
            ),
    )

    log.info("jOOQ code generation completed successfully!")
    log.info("Generated files in: $outputDir")
}
