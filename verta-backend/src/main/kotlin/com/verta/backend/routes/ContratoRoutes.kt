package com.verta.backend.routes

import com.verta.backend.dto.ContratoCreateDto
import com.verta.backend.dto.ContratoStatusUpdateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.GerarContratoDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.plugins.empresaIdLogado
import com.verta.backend.plugins.isAdminLogado
import com.verta.backend.plugins.usuarioIdLogado
import com.verta.backend.repositories.ContratoRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta as telas "Dashboard > Contratos recentes" e "Geracao de Contrato".
// empresaId/usuarioId sempre vem do JWT (nunca do payload do cliente) para
// impedir que um usuario acesse ou crie contratos de outra empresa/pessoa.
fun Route.contratoRoutes() {
    route("/contratos") {
        get {
            val status = call.request.queryParameters["status"]
            call.respond(
                ContratoRepository.findAll(
                    empresaId = call.empresaIdLogado(),
                    status = status,
                    usuarioId = call.usuarioIdLogado(),
                    isAdmin = call.isAdminLogado()
                )
            )
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val contrato = ContratoRepository.findById(
                id, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado()
            ) ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))

            call.respond(contrato)
        }

        post {
            val body = call.receive<ContratoCreateDto>()
                .copy(empresaId = call.empresaIdLogado(), criadoPor = call.usuarioIdLogado())
            call.respond(HttpStatusCode.Created, ContratoRepository.create(body))
        }

        // Usado pela tela "Geracao de Contrato": template + valores preenchidos -> contrato criado.
        post("/gerar") {
            val body = call.receive<GerarContratoDto>()
                .copy(empresaId = call.empresaIdLogado(), criadoPor = call.usuarioIdLogado())
            val contrato = ContratoRepository.generateFromTemplate(body)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            call.respond(HttpStatusCode.Created, contrato)
        }

        patch("/{id}/status") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val permissoes = ContratoRepository.permissoesDe(id, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado())
                ?: return@patch call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            if (!permissoes.editar && !permissoes.assinar) {
                return@patch call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para alterar este contrato"))
            }

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

            val permissoes = ContratoRepository.permissoesDe(id, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado())
                ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            if (!permissoes.excluir) {
                return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para excluir este contrato"))
            }

            val deleted = ContratoRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            }
            call.respond(MessageResponseDto("Contrato removido com sucesso"))
        }
    }
}
