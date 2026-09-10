package com.verta.backend.routes

import com.verta.backend.dto.ContratoUsuarioCreateDto
import com.verta.backend.dto.ContratoUsuarioPermissoesUpdateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.repositories.ContratoUsuarioRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Controla quem pode ver/editar/assinar/excluir cada contrato.
fun Route.contratoUsuarioRoutes() {
    route("/contrato-usuarios") {
        get {
            val contratoId = call.request.queryParameters["contratoId"]?.toIntOrNull()
            val usuarioId = call.request.queryParameters["usuarioId"]?.toIntOrNull()
            when {
                contratoId != null -> call.respond(ContratoUsuarioRepository.findByContrato(contratoId))
                usuarioId != null -> call.respond(ContratoUsuarioRepository.findByUsuario(usuarioId))
                else -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponseDto("Informe contratoId ou usuarioId na query string")
                )
            }
        }

        post {
            val body = call.receive<ContratoUsuarioCreateDto>()
            call.respond(HttpStatusCode.Created, ContratoUsuarioRepository.create(body))
        }

        put("/{id}/permissoes") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<ContratoUsuarioPermissoesUpdateDto>()
            val updated = ContratoUsuarioRepository.updatePermissoes(id, body)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))
            }
            call.respond(MessageResponseDto("Permissoes atualizadas com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = ContratoUsuarioRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))
            }
            call.respond(MessageResponseDto("Vinculo removido com sucesso"))
        }
    }
}
