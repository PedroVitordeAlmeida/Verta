package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.dto.UsuarioCreateDto
import com.verta.backend.dto.UsuarioUpdateDto
import com.verta.backend.repositories.UsuarioRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.usuarioRoutes() {
    route("/usuarios") {
        get {
            val empresaId = call.request.queryParameters["empresaId"]?.toIntOrNull()
            call.respond(UsuarioRepository.findAll(empresaId))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val usuario = UsuarioRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))

            call.respond(usuario)
        }

        post {
            val body = call.receive<UsuarioCreateDto>()
            call.respond(HttpStatusCode.Created, UsuarioRepository.create(body))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<UsuarioUpdateDto>()
            val updated = UsuarioRepository.update(id, body)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))
            }
            call.respond(MessageResponseDto("Usuario atualizado com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = UsuarioRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))
            }
            call.respond(MessageResponseDto("Usuario removido com sucesso"))
        }
    }
}
