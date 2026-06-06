package com.sportanalytics.service.di

import com.fasterxml.jackson.databind.ObjectMapper
import com.sportanalytics.core.etl.ETLService
import com.sportanalytics.core.etl.ValidationService
import com.sportanalytics.core.infrastructure.ClickHouseClient
import com.sportanalytics.core.infrastructure.RedisCache
import com.sportanalytics.core.logging.EventLogger
import com.sportanalytics.core.metrics.MetricsService
import com.sportanalytics.core.prediction.DixonColesModel
import com.sportanalytics.core.rating.RatingService
import com.sportanalytics.core.repository.*
import com.sportanalytics.core.scheduler.SchedulerService
import com.sportanalytics.core.seed.SeedDataService
import com.sportanalytics.database.DatabaseFactory
import com.sportanalytics.di.autoBind
import com.sportanalytics.environment.ContainerVariables
import com.sportanalytics.jackson.createObjectMapper
import com.sportanalytics.migration.MigrationRunner
import com.sportanalytics.service.*
import com.sportanalytics.service.auth.AuthService
import org.jooq.DSLContext
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val log = LoggerFactory.getLogger("com.sportanalytics.service.di.Container")

fun createSportAnalyticsContainer(variables: ContainerVariables): DI = DI {

    bind<ContainerVariables>() with singleton { variables }

    bind<ObjectMapper>() with singleton { createObjectMapper() }

    // Database
    bind<DataSource>() with singleton {
        DatabaseFactory.createDataSource(
            jdbcUrl = variables.get(DATABASE_URL),
            username = variables.get(DATABASE_USER),
            password = variables.get(DATABASE_PASSWORD),
            poolSize = variables.getOrDefault(DATABASE_POOL_SIZE, 20),
            poolName = "sport-analytics-pool",
        )
    }

    bind<DSLContext>() with singleton {
        DatabaseFactory.createDSLContext(instance())
    }

    // Migrations
    bind<Unit>(tag = "migrations") with singleton {
        try {
            MigrationRunner.run(
                jdbcUrl = variables.get(DATABASE_URL),
                username = variables.get(DATABASE_USER),
                password = variables.get(DATABASE_PASSWORD),
            )
        } catch (e: Exception) {
            log.warn("Migration failed: {}", e.message)
        }
    }

    // Repositories (autoBind resolves DSLContext from container)
    autoBind<LeagueRepository>()
    autoBind<TeamRepository>()
    autoBind<MatchRepository>()
    autoBind<MatchStatsRepository>()
    autoBind<TeamRatingRepository>()
    autoBind<PredictionRepository>()
    autoBind<UserRepository>()
    autoBind<EventLogRepository>()

    // Infrastructure (OLAP storage + cache, diploma Лист 1)
    bind<ClickHouseClient>() with singleton {
        ClickHouseClient(
            baseUrl = variables.getOrDefault(CLICKHOUSE_URL, "http://localhost:8123"),
            user = variables.getOrDefault(CLICKHOUSE_USER, "sa_user"),
            password = variables.getOrDefault(CLICKHOUSE_PASSWORD, "sa_password"),
        )
    }

    bind<RedisCache>() with singleton {
        RedisCache(variables.getOrDefault(REDIS_URL, "redis://localhost:6379"))
    }

    // Services
    autoBind<RatingService>()
    autoBind<SeedDataService>()
    autoBind<EventLogger>()
    autoBind<ValidationService>()
    autoBind<MetricsService>()
    autoBind<ETLService>()
    autoBind<SchedulerService>()

    bind<AuthService>() with singleton {
        AuthService(variables.getOrDefault(JWT_SECRET, "sport-analytics-jwt-secret-change-in-production"))
    }

    // Prediction model
    bind<DixonColesModel>() with singleton { DixonColesModel() }
}
