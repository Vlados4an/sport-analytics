package com.sportanalytics.service.route

import com.sportanalytics.core.repository.UserRepository
import com.sportanalytics.di.justInstance
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

data class RegisterRequest(val login: String, val password: String)
data class LoginRequest(val login: String, val password: String)

private val JWT_SECRET: SecretKey = Keys.hmacShaKeyFor(
    "sport-analytics-jwt-secret-key-minimum-256-bits-long!!".toByteArray(),
)
private const val ACCESS_TOKEN_MINUTES = 30L
private const val REFRESH_TOKEN_DAYS = 7L

private fun generateToken(login: String, role: String, expiration: Instant): String =
    Jwts.builder()
        .subject(login)
        .claim("role", role)
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(expiration))
        .signWith(JWT_SECRET)
        .compact()

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

            val now = Instant.now()
            val accessToken = generateToken(user.login, user.role, now.plus(ACCESS_TOKEN_MINUTES, ChronoUnit.MINUTES))
            val refreshToken = generateToken(user.login, user.role, now.plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS))

            call.respond(
                mapOf(
                    "status" to "ok",
                    "login" to user.login,
                    "role" to user.role,
                    "accessToken" to accessToken,
                    "refreshToken" to refreshToken,
                    "expiresIn" to ACCESS_TOKEN_MINUTES * 60,
                ),
            )
        }
    }
}
