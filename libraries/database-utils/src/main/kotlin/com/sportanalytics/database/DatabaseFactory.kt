package com.sportanalytics.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {

    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun createDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
        poolSize: Int = 20,
        poolName: String = "sport-analytics-pool",
    ): HikariDataSource {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            maximumPoolSize = poolSize
            this.poolName = poolName
            isAutoCommit = true
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
        }

        return HikariDataSource(config).also {
            log.info("HikariCP pool '$poolName' created: $jdbcUrl")
        }
    }

    fun createDSLContext(dataSource: DataSource): DSLContext =
        DSL.using(dataSource, SQLDialect.POSTGRES)

    fun runMigrations(dataSource: DataSource, locations: String = "classpath:db/migration") {
        val result = Flyway.configure()
            .dataSource(dataSource)
            .locations(locations)
            .load()
            .migrate()
        log.info("Flyway migrations applied (result=$result)")
    }
}
