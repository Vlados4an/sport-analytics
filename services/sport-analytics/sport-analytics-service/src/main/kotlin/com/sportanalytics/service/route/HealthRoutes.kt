package com.sportanalytics.service.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Route.healthRoutes() {
    get("/api/health") {
        call.respond(mapOf("status" to "ok", "service" to "sport-analytics", "timestamp" to Instant.now().toString()))
    }
}
