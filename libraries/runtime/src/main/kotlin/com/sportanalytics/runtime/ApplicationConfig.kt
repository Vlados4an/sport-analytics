package com.sportanalytics.runtime

import com.sportanalytics.environment.ContainerVariables
import io.ktor.server.engine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.slf4j.LoggerFactory

/**
 * Application bootstrap pattern from mono-repo.
 * suspend fun main() = createApplicationConfig { ... }.run()
 */

data class ContainerArgs(
    val variables: ContainerVariables,
    val scope: CoroutineScope,
)

data class RunArgs(
    val variables: ContainerVariables,
    val container: DI,
)

class ApplicationConfig {
    var serviceName: String = "sport-analytics"
    var port: Int = 8080

    var containerVariables: (() -> ContainerVariables)? = null

    var container: (suspend CoroutineScope.(args: ContainerArgs) -> DI)? = null

    var engine: ((port: Int, DI) -> ApplicationEngine)? = null

    var run: suspend CoroutineScope.(args: RunArgs) -> Unit = {}

    suspend fun run() {
        val log = LoggerFactory.getLogger("com.sportanalytics.runtime.ApplicationConfig.[$serviceName]")

        withContext(Dispatchers.Default) {
            try {
                val variables = containerVariables?.invoke() ?: ContainerVariables()
                log.info("Container variables:\n${variables.debugString()}")

                val di = container?.invoke(this, ContainerArgs(variables, this))
                    ?: error("Container not provided")

                run(this, RunArgs(variables, di))

                val appEngine = engine?.invoke(port, di)
                if (appEngine != null) {
                    appEngine.start(wait = false)
                    log.info("[UP] [$serviceName] HTTP server listen port: $port")
                }

                coroutineContext[Job]!!.join()
            } catch (t: Throwable) {
                log.error("[APPLICATION_ERROR] ${t.message}", t)
                throw t
            }
        }
    }
}

fun createApplicationConfig(block: ApplicationConfig.() -> Unit): ApplicationConfig =
    ApplicationConfig().apply(block)
