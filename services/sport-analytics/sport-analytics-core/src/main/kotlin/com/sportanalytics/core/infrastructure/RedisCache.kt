package com.sportanalytics.core.infrastructure

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Cache-aside layer over Redis (diploma 5.2). All operations fail soft:
 * if Redis is unavailable the system keeps serving from PostgreSQL.
 */
class RedisCache(redisUrl: String) {

    private val log = LoggerFactory.getLogger(RedisCache::class.java)

    private val client: RedissonClient? = try {
        val config = Config()
        config.useSingleServer()
            .setAddress(redisUrl)
            .setConnectTimeout(3000)
            .setTimeout(3000)
        Redisson.create(config)
    } catch (e: Exception) {
        log.warn("Redis unavailable, cache disabled: {}", e.message)
        null
    }

    fun ping(): Boolean {
        val redisson = client ?: return false

        return try {
            redisson.getBucket<String>("health:ping").isExists
            true
        } catch (_: Exception) {
            false
        }
    }

    fun get(key: String): String? =
        try {
            client?.getBucket<String>(key)?.get()
        } catch (e: Exception) {
            log.warn("Redis GET failed for {}: {}", key, e.message)
            null
        }

    fun set(key: String, value: String, ttl: Duration = DEFAULT_TTL) {
        try {
            client?.getBucket<String>(key)?.set(value, ttl)
        } catch (e: Exception) {
            log.warn("Redis SET failed for {}: {}", key, e.message)
        }
    }

    fun invalidate(pattern: String): Long =
        try {
            client?.keys?.deleteByPattern(pattern) ?: 0L
        } catch (e: Exception) {
            log.warn("Redis invalidate failed for {}: {}", pattern, e.message)
            0L
        }

    fun shutdown() {
        client?.shutdown()
    }

    companion object {
        val DEFAULT_TTL: Duration = Duration.ofMinutes(10)
    }
}
