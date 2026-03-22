package com.sportanalytics.service

import com.sportanalytics.di.justInstance
import com.sportanalytics.jackson.createObjectMapper
import com.sportanalytics.service.di.createSportAnalyticsContainer
import com.sportanalytics.service.route.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.sportanalytics.service.Application")

suspend fun main() {
    val variables = createProductionContainerVariables()
    val container = createSportAnalyticsContainer(variables)

    // Trigger migrations
    container.justInstance<Unit>(tag = "migrations")

    val port: Int = variables.getOrDefault(SERVICE_PORT, 8080)

    log.info("[STARTING] sport-analytics on port $port")

    embeddedServer(Netty, port = port) {
        configureApplication(container)
    }.start(wait = true)
}

fun Application.configureApplication(di: DI) {
    install(ContentNegotiation) {
        jackson { createObjectMapper().let { m ->
            registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }}
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(CallLogging)

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
        }
        exception<Exception> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
        }
    }

    routing {
        healthRoutes()
        leagueRoutes(di)
        teamRoutes(di)
        matchRoutes(di)
        ratingRoutes(di)
        predictionRoutes(di)
        authRoutes(di)
        adminRoutes(di)
    }

    log.info("[UP] sport-analytics API ready")
}
