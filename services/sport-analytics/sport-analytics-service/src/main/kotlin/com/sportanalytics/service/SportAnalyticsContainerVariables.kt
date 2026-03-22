package com.sportanalytics.service

import com.sportanalytics.environment.ContainerVariables

const val SERVICE_NAME = "SERVICE_NAME"
const val SERVICE_PORT = "SERVICE_PORT"
const val DATABASE_URL = "DATABASE_URL"
const val DATABASE_USER = "DATABASE_USER"
const val DATABASE_PASSWORD = "DATABASE_PASSWORD"
const val DATABASE_POOL_SIZE = "DATABASE_POOL_SIZE"
const val CLICKHOUSE_URL = "CLICKHOUSE_URL"
const val REDIS_URL = "REDIS_URL"
const val JWT_SECRET = "JWT_SECRET"
const val API_FOOTBALL_KEY = "API_FOOTBALL_KEY"

fun createProductionContainerVariables() = ContainerVariables().apply {
    set(SERVICE_NAME, "sport-analytics")
    set(SERVICE_PORT, 8080)
    set(DATABASE_URL, "jdbc:postgresql://localhost:5432/sport_analytics")
    set(DATABASE_USER, "sa_user")
    set(DATABASE_PASSWORD, "sa_password")
    set(DATABASE_POOL_SIZE, 20)
    set(CLICKHOUSE_URL, "http://localhost:8123")
    set(REDIS_URL, "redis://localhost:6379")
    set(JWT_SECRET, "sport-analytics-jwt-secret-change-in-production")
    set(API_FOOTBALL_KEY, "")
}

fun createDevelopmentContainerVariables() = createProductionContainerVariables().apply {
    set(DATABASE_POOL_SIZE, 5)
}
