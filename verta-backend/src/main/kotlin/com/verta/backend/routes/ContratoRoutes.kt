package com.verta.backend.routes

import com.verta.backend.dto.ContratoCreateDto
import com.verta.backend.dto.ContratoStatusUpdateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.GerarContratoDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.repositories.ContratoRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta as telas "Dashboard > Contratos recentes" e "Geracao de Contrato".
fun Route.contratoRoutes() {
    route("/contratos") {
        get {
            val empresaId = call.request.queryParameters["empresaId"]?.toIntOrNull()
            val status = call.request.queryParameters["status"]
            call.respond(ContratoRepository.findAll(empresaId, status))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val contrato = ContratoRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))

            call.respond(contrato)
        }

        post {
            val body = call.receive<ContratoCreateDto>()
            call.respond(HttpStatusCode.Created, ContratoRepository.create(body))
        }

        // Usado pela tela "Geracao de Contrato": template + valores preenchidos -> contrato criado.
        post("/gerar") {
            val body = call.receive<GerarContratoDto>()
            val contrato = ContratoRepository.generateFromTemplate(body)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            call.respond(HttpStatusCode.Created, contrato)
        }

        patch("/{id}/status") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<ContratoStatusUpdateDto>()
            val updated = ContratoRepository.updateStatus(id, body.status)
            if (!updated) {
                return@patch call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            }
            call.respond(MessageResponseDto("Status do contrato atualizado com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = ContratoRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            }
            call.respond(MessageResponseDto("Contrato removido com sucesso"))
        }
    }
}
