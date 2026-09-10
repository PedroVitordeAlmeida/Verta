package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.VersaoContratoCreateDto
import com.verta.backend.repositories.VersaoContratoRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Historico de versoes de um contrato (edicoes salvas na tela "Geracao de Contrato").
fun Route.versaoContratoRoutes() {
    route("/versoes-contrato") {
        get {
            val contratoId = call.request.queryParameters["contratoId"]?.toIntOrNull()
            if (contratoId != null) {
                call.respond(VersaoContratoRepository.findByContrato(contratoId))
            } else {
                call.respond(VersaoContratoRepository.findAll())
            }
        }

        post {
            val body = call.receive<VersaoContratoCreateDto>()
            call.respond(HttpStatusCode.Created, VersaoContratoRepository.create(body))
        }
    }
}
