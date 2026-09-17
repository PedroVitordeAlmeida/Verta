package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.dto.UsuarioCreateDto
import com.verta.backend.dto.UsuarioUpdateDto
import com.verta.backend.plugins.empresaIdLogado
import com.verta.backend.plugins.isAdminLogado
import com.verta.backend.repositories.UsuarioRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Gerenciamento de usuarios da empresa. Listar/ver e liberado a qualquer usuario
 * logado (para exibir a equipe), mas criar/editar/excluir e restrito a ADMIN -
 * e sempre dentro da propria empresa do usuario logado, nunca de outra.
 */
fun Route.usuarioRoutes() {
    route("/usuarios") {
        get {
            call.respond(UsuarioRepository.findAll(call.empresaIdLogado()))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val usuario = UsuarioRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))

            call.respond(usuario)
        }

        post {
            if (!call.isAdminLogado()) {
                return@post call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas administradores podem cadastrar usuarios"))
            }

            val body = call.receive<UsuarioCreateDto>().copy(empresaId = call.empresaIdLogado())
            call.respond(HttpStatusCode.Created, UsuarioRepository.create(body))
        }

        put("/{id}") {
            if (!call.isAdminLogado()) {
                return@put call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas administradores podem editar usuarios"))
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val alvo = UsuarioRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))

            val body = call.receive<UsuarioUpdateDto>()
            UsuarioRepository.update(alvo.id!!, body)
            call.respond(MessageResponseDto("Usuario atualizado com sucesso"))
        }

        delete("/{id}") {
            if (!call.isAdminLogado()) {
                return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas administradores podem remover usuarios"))
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val alvo = UsuarioRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Usuario nao encontrado"))

            UsuarioRepository.delete(alvo.id!!)
            call.respond(MessageResponseDto("Usuario removido com sucesso"))
        }
    }
}
