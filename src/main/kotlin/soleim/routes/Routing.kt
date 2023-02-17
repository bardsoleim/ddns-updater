package soleim.routes

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import soleim.dto.Domain
import soleim.db.dao


fun Application.configureRouting() {
    routing {
        route("domain") {
            get() {
                call.respond(mapOf("domains" to dao.getAll()))
            }
            post("/add") {
                dao.add(call.receive<Domain>().apply {
                    this.ip =
                        this.ip.takeUnless { it.isNullOrEmpty() } ?: call.request.headers["X-Forwarded-For"]?.split(",")
                            ?.firstOrNull()?.trim() ?: call.request.origin.remoteHost
                })
                call.respond(HttpStatusCode.Created)
            }
            delete {
                dao.delete(call.receiveParameters().getOrFail("domain"))
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
