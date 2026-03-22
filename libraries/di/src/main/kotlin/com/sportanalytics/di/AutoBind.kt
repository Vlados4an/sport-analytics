package com.sportanalytics.di

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.kodein.type.typeToken
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.javaType

/**
 * Simplified autoBind from mono-repo.
 * Automatically resolves constructor dependencies from DI container.
 */
inline fun <reified T : Any> DI.Builder.autoBind(
    tag: Any? = null,
    overrides: Boolean? = null,
) {
    bind<T>(tag = tag, overrides = overrides) with singleton {
        directAutoBind<T>(directDI)
    }
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> directAutoBind(di: DirectDI): T {
    val clazz = T::class
    val constructor = clazz.primaryConstructor
        ?: clazz.objectInstance?.let { return it }
        ?: error("No primary constructor for ${clazz.simpleName}")

    return constructor.callBy(
        buildMap(constructor.parameters.size) {
            constructor.parameters.forEach { param ->
                val value = try {
                    @Suppress("UNCHECKED_CAST")
                    val token = typeToken(param.type.javaType) as org.kodein.type.TypeToken<Any>
                    di.Instance(token, tag = null)
                } catch (t: Throwable) {
                    if (param.isOptional) return@forEach
                    throw RuntimeException(
                        "AutoBind for ${clazz.simpleName} failed. " +
                            "Parameter '${param.name}' not resolved: ${t.message}",
                        t,
                    )
                }
                put(param, value)
            }
        },
    )
}
