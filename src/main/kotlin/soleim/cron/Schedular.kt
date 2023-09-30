package soleim.cron
import kotlinx.coroutines.*
import soleim.db.dao
import io.ktor.client.request.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*

val ioScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

fun startScheduler() {
    ioScope.launch {
        while (ioScope.isActive) {
            println("updating ddns...")
            dao.getAll().forEach { domain ->
                ioScope.launch {
                    HttpClient(Apache).use {
                        it.get(domain.dnsProvider) {
                            url {
                                domain.ip?.let {parameters.append("host", domain.ip!!) }
                                parameters.append("domain", domain.domain)
                                parameters.append("password", domain.password)
                            }
                        }
                        it.close()
                    }
                }

            }

            delay(1000)
        }
    }
}

