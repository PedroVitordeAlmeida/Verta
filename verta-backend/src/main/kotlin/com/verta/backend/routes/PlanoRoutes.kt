package com.verta.backend.routes

import com.verta.backend.repositories.PlanoRepository
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

/** Catalogo fixo de planos (Basico/Plus) - leitura liberada a qualquer usuario logado. */
fun Route.planoRoutes() {
    route("/planos") {
        get {
            call.respond(PlanoRepository.findAll())
        }
    }
}
