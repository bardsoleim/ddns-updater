package soleim

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import soleim.db.DatabaseFactory
import soleim.routes.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.partialcontent.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import soleim.cron.startScheduler
import soleim.dto.Domain

fun main() {

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = SerializersModule {
                contextual(Domain::class, Domain.serializer())
            }
        })
    }
    install(PartialContent)
    install(AutoHeadResponse)
    DatabaseFactory.init()
    startScheduler()
    configureRouting()
}
