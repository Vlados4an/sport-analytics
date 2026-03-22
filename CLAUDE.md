# CLAUDE.md

## Build Commands

All commands from project root `C:\Users\user\Desktop\sport-analytics\`.

```bash
# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Compile all
.\gradlew.bat compileKotlin

# Test (core prediction engine)
.\gradlew.bat :services:sport-analytics:sport-analytics-core:test

# Build fat JAR
.\gradlew.bat :services:sport-analytics:sport-analytics-service:shadowJar

# Run service (needs DB)
.\gradlew.bat :services:sport-analytics:sport-analytics-service:run

# Docker (everything)
docker compose up -d --build
```

## Module Structure

```
libraries/
  di/                    # autoBind, justInstance, Dependency (from mono-repo pattern)
  noarg/                 # @NoArg annotation
  environment/           # ContainerVariables
  runtime/               # ApplicationConfig (createApplicationConfig().run())
  jackson-config/        # createObjectMapper()
  database-utils/        # DatabaseFactory (HikariCP + Flyway + jOOQ)

services/sport-analytics/
  sport-analytics-api/        # DTOs
  sport-analytics-core/       # Domain: prediction, rating, repositories
  sport-analytics-migration/  # Flyway SQL + MigrationRunner
  sport-analytics-service/    # Entry point, DI container, Ktor routes
```

## Entry Point Pattern

```kotlin
suspend fun main() {
    val variables = createProductionContainerVariables()
    val container = createSportAnalyticsContainer(variables)
    // start Ktor with container
}
```

## Key Conventions

- **Package**: `com.sportanalytics.<module>`
- **DI**: Kodein with `autoBind<T>()` and `justInstance<T>()`
- **DB**: jOOQ DSL (inline field references), HikariCP pool
- **Migrations**: Flyway SQL in `sport-analytics-migration/src/main/resources/db/migration/`
- **Tests**: Kotest FunSpec
- **Dependencies**: `gradle/libs.versions.toml` (never inline versions)
