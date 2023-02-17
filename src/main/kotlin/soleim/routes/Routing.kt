package soleim.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing() {
       domain()
    }
}
