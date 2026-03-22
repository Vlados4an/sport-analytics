package com.sportanalytics.di

import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

inline fun <reified T : Any> DI.justInstance(tag: Any? = null): T {
    val inst by instance<T>(tag)
    return inst
}

inline fun <reified T : Any> DI.justInstances(tag: Any? = null): List<T> {
    val inst by allInstances<T>(tag)
    return inst
}

inline fun <reified T : Any> DI.justInstanceOrNull(tag: Any? = null): T? {
    val inst by instanceOrNull<T>(tag)
    return inst
}
