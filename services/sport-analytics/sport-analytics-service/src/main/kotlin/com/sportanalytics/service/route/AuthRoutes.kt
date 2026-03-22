package com.sportanalytics.service.route

import com.sportanalytics.core.repository.UserRepository
import com.sportanalytics.di.justInstance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI

data class RegisterRequest(val login: String, val password: String)
data class LoginRequest(val login: String, val password: String)

fun Route.authRoutes(di: DI) {
    val userRepo = di.justInstance<UserRepository>()

    route("/api/auth") {
        post("/register") {
            val body = call.receive<RegisterRequest>()
            require(body.login.length >= 3) { "Login min 3 chars" }
            require(body.password.length >= 6) { "Password min 6 chars" }
            require(!userRepo.existsByLogin(body.login)) { "User exists" }

            val hash = org.mindrot.jbcrypt.BCrypt.hashpw(body.password, org.mindrot.jbcrypt.BCrypt.gensalt(10))
            userRepo.insert(com.sportanalytics.core.model.User(login = body.login, passwordHash = hash))

            call.respond(HttpStatusCode.Created, mapOf("status" to "registered", "login" to body.login))
        }

        post("/login") {
            val body = call.receive<LoginRequest>()
            val user = userRepo.findByLogin(body.login)
                ?: throw IllegalArgumentException("Invalid credentials")

            require(org.mindrot.jbcrypt.BCrypt.checkpw(body.password, user.passwordHash)) { "Invalid credentials" }

            call.respond(mapOf("status" to "ok", "login" to user.login, "role" to user.role))
        }
    }
}
