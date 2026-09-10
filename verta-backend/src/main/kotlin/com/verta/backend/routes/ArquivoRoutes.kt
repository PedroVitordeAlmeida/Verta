package com.verta.backend.routes

import com.verta.backend.dto.ArquivoCreateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.repositories.ArquivoRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta a tela "Gerenciador de Arquivos" (contratos gerados, assinados, arquivados).
// Cada arquivo agora pertence a uma versao especifica do contrato (versao_id).
// O upload fisico do binario pode ser feito por outro servico (ex: storage/S3);
// aqui persistimos apenas os metadados na tabela public.arquivos.
fun Route.arquivoRoutes() {
    route("/arquivos") {
        get {
            val contratoId = call.request.queryParameters["contratoId"]?.toIntOrNull()
            val versaoId = call.request.queryParameters["versaoId"]?.toIntOrNull()
            when {
                versaoId != null -> call.respond(ArquivoRepository.findByVersao(versaoId))
                contratoId != null -> call.respond(ArquivoRepository.findByContrato(contratoId))
                else -> call.respond(ArquivoRepository.findAll())
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val arquivo = ArquivoRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Arquivo nao encontrado"))

            call.respond(arquivo)
        }

        post {
            val body = call.receive<ArquivoCreateDto>()
            call.respond(HttpStatusCode.Created, ArquivoRepository.create(body))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val deleted = ArquivoRepository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Arquivo nao encontrado"))
            }
            call.respond(MessageResponseDto("Arquivo removido com sucesso"))
        }
    }
}
