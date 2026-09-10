package com.verta.backend.routes

import com.verta.backend.dto.MessageResponseDto
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Usado pelo healthcheck do docker-compose.
fun Route.healthRoutes() {
    get("/health") {
        call.respond(MessageResponseDto("ok"))
    }
}
