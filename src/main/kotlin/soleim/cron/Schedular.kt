package soleim.cron

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import soleim.db.dao

val ioScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

private val lastUpdatedIp = mutableMapOf<String, String>()

private suspend fun fetchPublicIp(client: HttpClient): String? = runCatching {
    client.get("https://api.ipify.org").bodyAsText().trim()
}.getOrNull()

fun startScheduler() {
    ioScope.launch {
        while (ioScope.isActive) {
            runCatching {
                HttpClient(Apache).use { client ->
                    val publicIp = fetchPublicIp(client)
                    dao.getAll().forEach { domain ->
                        val ip = domain.ip ?: publicIp ?: return@forEach
                        if (lastUpdatedIp[domain.domain] == ip) return@forEach
                        runCatching {
                            client.get(domain.dnsProvider) {
                                url {
                                    parameters.append("host", domain.host)
                                    parameters.append("domain", domain.domain)
                                    parameters.append("password", domain.password)
                                    parameters.append("ip", ip)
                                }
                            }
                            lastUpdatedIp[domain.domain] = ip
                            println("Updated ${domain.domain} (host=${domain.host}) to $ip")
                        }.onFailure { println("Failed to update ${domain.domain}: ${it.message}") }
                    }
                }
            }.onFailure { println("Scheduler error: ${it.message}") }
            delay(5 * 60 * 1000L)
        }
    }
}

