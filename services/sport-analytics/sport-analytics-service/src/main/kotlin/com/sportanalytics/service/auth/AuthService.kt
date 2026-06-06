package com.sportanalytics.service.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

/**
 * JWT authentication service (diploma 5.2, Лист 5): issues access/refresh
 * token pairs, validates tokens and enforces the password policy.
 */
class AuthService(jwtSecret: String) {

    data class TokenPair(val accessToken: String, val refreshToken: String, val expiresIn: Long)

    private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.padEnd(MIN_SECRET_LENGTH, '#').toByteArray())

    fun issueTokens(login: String, role: String): TokenPair {
        val now = Instant.now()

        return TokenPair(
            accessToken = generateToken(login, role, "access", now.plus(ACCESS_TOKEN_MINUTES, ChronoUnit.MINUTES)),
            refreshToken = generateToken(login, role, "refresh", now.plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS)),
            expiresIn = ACCESS_TOKEN_MINUTES * 60,
        )
    }

    fun validate(token: String): Claims? =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (_: Exception) {
            null
        }

    fun refresh(refreshToken: String): TokenPair? {
        val claims = validate(refreshToken) ?: return null

        if (claims["type"] != "refresh") return null

        return issueTokens(claims.subject, claims["role"] as? String ?: "user")
    }

    fun checkPasswordPolicy(password: String): String? = when {
        password.length < MIN_PASSWORD_LENGTH ->
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        !password.any { it.isLetter() } || !password.any { it.isDigit() } ->
            "Password must contain both letters and digits"
        else -> null
    }

    private fun generateToken(login: String, role: String, type: String, expiration: Instant): String =
        Jwts.builder()
            .subject(login)
            .claim("role", role)
            .claim("type", type)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact()

    companion object {
        const val ACCESS_TOKEN_MINUTES = 30L
        const val REFRESH_TOKEN_DAYS = 7L
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_SECRET_LENGTH = 32
    }
}
