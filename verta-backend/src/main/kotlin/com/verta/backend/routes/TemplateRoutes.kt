package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.dto.TemplateCreateDto
import com.verta.backend.repositories.TemplateRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta as telas "Gerenciador de Arquivos > Templates" e "Geracao de Contrato".
fun Route.templateRoutes() {
    route("/templates") {
        get {
            val empresaId = call.request.queryParameters["empresaId"]?.toIntOrNull()
            call.respond(TemplateRepository.findAll(empresaId))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val template = TemplateRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            call.respond(template)
        }

        post {
            val body = call.receive<TemplateCreateDto>()
            call.respond(HttpStatusCode.Created, TemplateRepository.create(body))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<TemplateCreateDto>()
            val updated = TemplateRepository.update(id, body)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))
            }
            call.respond(MessageResponseDto("Template atualizado com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = TemplateRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))
            }
            call.respond(MessageResponseDto("Template removido com sucesso"))
        }
    }
}
