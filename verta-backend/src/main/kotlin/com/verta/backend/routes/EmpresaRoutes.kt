package com.verta.backend.routes

import com.verta.backend.dto.EmpresaAtribuirPlanoDto
import com.verta.backend.dto.EmpresaCreateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.plugins.isSuperAdminLogado
import com.verta.backend.repositories.EmpresaRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.empresaRoutes() {
    route("/empresas") {
        get {
            call.respond(EmpresaRepository.findAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val empresa = EmpresaRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Empresa nao encontrada"))

            call.respond(empresa)
        }

        post {
            val body = call.receive<EmpresaCreateDto>()
            call.respond(HttpStatusCode.Created, EmpresaRepository.create(body))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<EmpresaCreateDto>()
            val updated = EmpresaRepository.update(id, body)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Empresa nao encontrada"))
            }
            call.respond(MessageResponseDto("Empresa atualizada com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = EmpresaRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Empresa nao encontrada"))
            }
            call.respond(MessageResponseDto("Empresa removida com sucesso"))
        }

        // Atribui/troca o plano (e periodo) de uma empresa cliente. Restrito a SUPERADMIN
        // (equipe Verta/Dalcomad) - nao e algo que a propria empresa cliente pode se auto-atribuir.
        put("/{id}/plano") {
            if (!call.isSuperAdminLogado()) {
                return@put call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas a equipe Verta pode atribuir planos"))
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val body = call.receive<EmpresaAtribuirPlanoDto>()
            val empresa = EmpresaRepository.atribuirPlano(id, body.planoId, body.periodo)
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Empresa nao encontrada"))

            call.respond(empresa)
        }
    }
}
