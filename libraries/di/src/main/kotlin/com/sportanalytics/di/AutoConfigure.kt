package com.sportanalytics.di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class Dependency(
    vararg defaults: Pair<String, Any?>,
) {
    val names: Set<String> get() = defaultsMap.keys
    private val defaultsMap = mapOf(*defaults)

    fun hasDefault(name: String, type: kotlin.reflect.KClass<Any>): Boolean {
        val value = defaultsMap[name] ?: return false
        return type.isInstance(value)
    }

    fun getDefault(name: String): Any? = defaultsMap[name]

    fun merge(other: Dependency): Dependency = Dependency(
        *defaultsMap.entries.map { it.toPair() }.toTypedArray(),
        *other.defaultsMap.map { it.toPair() }.toTypedArray(),
    )
}

fun DI.Builder.autoConfigure(vararg defaults: Pair<String, Any>) {
    bind { singleton { Dependency(*defaults) } }
}
