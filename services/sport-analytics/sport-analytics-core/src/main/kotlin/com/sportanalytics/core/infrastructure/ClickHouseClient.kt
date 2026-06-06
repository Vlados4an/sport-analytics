package com.sportanalytics.core.infrastructure

import com.sportanalytics.core.model.MatchMetrics
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * OLAP storage client (diploma 4.3): match_metrics table in ClickHouse.
 * Uses the HTTP interface with batch INSERT; ReplacingMergeTree deduplicates
 * by (team_id, match_id) so the sync is idempotent.
 */
class ClickHouseClient(
    private val baseUrl: String,
    private val user: String,
    private val password: String,
) {
    private val log = LoggerFactory.getLogger(ClickHouseClient::class.java)

    private val http = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

    fun ping(): Boolean =
        try {
            execute("SELECT 1") != null
        } catch (_: Exception) {
            false
        }

    fun ensureSchema(): Boolean {
        val ddl = """
            CREATE TABLE IF NOT EXISTS match_metrics (
                match_id      UInt32,
                team_id       UInt32,
                match_date    DateTime,
                xg            Float64,
                xa            Float64,
                xga           Float64,
                attack_index  Float64,
                defence_index Float64
            )
            ENGINE = ReplacingMergeTree
            PARTITION BY toYYYYMM(match_date)
            ORDER BY (team_id, match_id)
        """.trimIndent()

        return execute(ddl) != null
    }

    fun insertMetrics(rows: List<MatchMetrics>): Int {
        if (rows.isEmpty()) return 0

        val values = rows.joinToString(",") { m ->
            "(${m.matchId},${m.teamId},'${dateFormat.format(m.matchDate)}'," +
                "${m.xg},${m.xa},${m.xga},${m.attackIndex},${m.defenceIndex})"
        }
        val sql = "INSERT INTO match_metrics " +
            "(match_id, team_id, match_date, xg, xa, xga, attack_index, defence_index) VALUES $values"

        return if (execute(sql) != null) rows.size else 0
    }

    fun countMetrics(): Long =
        execute("SELECT count(*) FROM match_metrics")?.trim()?.toLongOrNull() ?: 0L

    fun avgMetrics(): Pair<Double, Double>? {
        val result = execute("SELECT avg(xg), avg(xga) FROM match_metrics") ?: return null
        val parts = result.trim().split("\t")

        if (parts.size != 2) return null

        val xg = parts[0].toDoubleOrNull() ?: return null
        val xga = parts[1].toDoubleOrNull() ?: return null

        return xg to xga
    }

    private fun execute(sql: String): String? =
        try {
            val request = Request.Builder()
                .url(baseUrl)
                .header("X-ClickHouse-User", user)
                .header("X-ClickHouse-Key", password)
                .post(sql.toRequestBody("text/plain".toMediaType()))
                .build()

            http.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string() ?: ""
                } else {
                    log.warn("ClickHouse query failed: {} {}", response.code, response.body?.string()?.take(200))
                    null
                }
            }
        } catch (e: Exception) {
            log.warn("ClickHouse unavailable: {}", e.message)
            null
        }
}
