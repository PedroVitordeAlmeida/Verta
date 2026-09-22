package com.verta.backend.plugins

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.services.TemplateAiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

/** Padroniza respostas de erro em JSON, evitando stacktraces vazando pra API. */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponseDto(cause.message ?: "Requisicao invalida"))
        }
        exception<TemplateAiException> { call, cause ->
            call.respond(HttpStatusCode.BadGateway, ErrorResponseDto(cause.message ?: "Falha ao gerar conteudo com IA"))
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponseDto(cause.message ?: "Recurso nao encontrado"))
        }
        exception<Throwable> { call, cause ->
            logger.error("Erro nao tratado", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponseDto("Erro interno no servidor"))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponseDto("Rota nao encontrada"))
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, ErrorResponseDto("Nao autenticado"))
        }
    }
}
