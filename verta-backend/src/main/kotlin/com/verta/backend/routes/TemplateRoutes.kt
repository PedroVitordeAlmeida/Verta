package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.dto.TemplateCreateDto
import com.verta.backend.dto.TemplateGeracaoIaRequestDto
import com.verta.backend.plugins.empresaIdLogado
import com.verta.backend.repositories.TemplateRepository
import com.verta.backend.services.TemplateAiService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta as telas "Gerenciador de Arquivos > Templates" e "Geracao de Contrato".
// Templates sao visiveis para toda a empresa (nao tem dono individual); empresaId
// sempre vem do JWT, nunca do payload do cliente, para nao vazar/alterar dados de outra empresa.
fun Route.templateRoutes(templateAiService: TemplateAiService) {
    route("/templates") {
        get {
            call.respond(TemplateRepository.findAll(call.empresaIdLogado()))
        }

        // Gera nome + conteudo de um template via IA a partir de uma descricao em linguagem
        // natural. Nao salva nada: o resultado volta para o usuario revisar/editar antes do
        // POST /templates de fato, evitando gravar conteudo indesejado direto no banco.
        post("/gerar-ia") {
            val body = call.receive<TemplateGeracaoIaRequestDto>()
            call.respond(templateAiService.gerar(body.descricao))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val template = TemplateRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            call.respond(template)
        }

        post {
            val body = call.receive<TemplateCreateDto>().copy(empresaId = call.empresaIdLogado())
            call.respond(HttpStatusCode.Created, TemplateRepository.create(body))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            TemplateRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            val body = call.receive<TemplateCreateDto>().copy(empresaId = call.empresaIdLogado())
            val updated = TemplateRepository.update(id, body)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))
            }
            call.respond(MessageResponseDto("Template atualizado com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            TemplateRepository.findById(id)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))

            val deleted = TemplateRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Template nao encontrado"))
            }
            call.respond(MessageResponseDto("Template removido com sucesso"))
        }
    }
}
