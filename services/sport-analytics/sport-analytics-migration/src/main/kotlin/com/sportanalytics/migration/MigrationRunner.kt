package com.sportanalytics.migration

import com.sportanalytics.database.DatabaseFactory
import org.slf4j.LoggerFactory

/**
 * Standalone migration runner — can be executed independently.
 * Reads DB config from env vars, runs Flyway migrations.
 */
object MigrationRunner {

    private val log = LoggerFactory.getLogger(MigrationRunner::class.java)

    fun run(
        jdbcUrl: String,
        username: String,
        password: String,
    ) {
        log.info("Running migrations against $jdbcUrl")

        val dataSource = DatabaseFactory.createDataSource(
            jdbcUrl = jdbcUrl,
            username = username,
            password = password,
            poolSize = 2,
            poolName = "migration-pool",
        )

        try {
            DatabaseFactory.runMigrations(dataSource)
            log.info("Migrations completed successfully")
        } finally {
            dataSource.close()
        }
    }
}

fun main() {
    MigrationRunner.run(
        jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/sport_analytics",
        username = System.getenv("DATABASE_USER") ?: "sa_user",
        password = System.getenv("DATABASE_PASSWORD") ?: "sa_password",
    )
}
