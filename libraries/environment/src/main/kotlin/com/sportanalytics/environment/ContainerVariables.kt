package com.sportanalytics.environment

import org.slf4j.LoggerFactory

class ContainerVariables {

    private val log = LoggerFactory.getLogger(ContainerVariables::class.java)
    @PublishedApi internal val values = mutableMapOf<String, Any?>()

    operator fun set(key: String, value: Any?) {
        values[key] = value
    }

    fun <T : Any> get(key: String): T {
        return getOrNull(key) ?: error("Variable '$key' not provided")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String): T? {
        val env = System.getenv(key)
        if (env != null && env.isNotBlank()) return env as? T

        return values[key] as? T
    }

    fun <T : Any> getOrDefault(key: String, default: T): T {
        return getOrNull(key) ?: default
    }

    fun debugString(): String = values.entries.joinToString("\n") { (k, v) ->
        val envValue = System.getenv(k)
        if (envValue != null) "  $k = $envValue (env)" else "  $k = $v (default)"
    }
}
