package soleim.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import soleim.dto.Domain
import soleim.db.dao


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("domains" to dao.getAll()))
        }
        post("/add") {
            dao.add(call.receive<Domain>().apply {
                this.ip = this.ip.takeUnless { it.isNullOrEmpty() } ?:
                call.request.headers["X-Forwarded-For"]?.split(",")?.firstOrNull()?.trim() ?:
                call.request.origin.remoteHost
            })
            call.respond(HttpStatusCode.OK)
        }
    }
}
