package com.verta.backend.routes

import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.VersaoContratoCreateDto
import com.verta.backend.plugins.empresaIdLogado
import com.verta.backend.plugins.isAdminLogado
import com.verta.backend.plugins.usuarioIdLogado
import com.verta.backend.repositories.ContratoRepository
import com.verta.backend.repositories.VersaoContratoRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Historico de versoes de um contrato (conteudo editado na tela "Geracao de Contrato"/detalhe do contrato).
// So acessivel a quem pode ver/editar o contrato (dono, ADMIN ou compartilhado via contrato_usuarios).
fun Route.versaoContratoRoutes() {
    route("/versoes-contrato") {
        get {
            val contratoId = call.request.queryParameters["contratoId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("Informe contratoId na query string"))

            val permissoes = ContratoRepository.permissoesDe(contratoId, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado())
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            if (!permissoes.visualizar) {
                return@get call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para ver este contrato"))
            }
            call.respond(VersaoContratoRepository.findByContrato(contratoId))
        }

        post {
            val body = call.receive<VersaoContratoCreateDto>()

            val permissoes = ContratoRepository.permissoesDe(body.contratoId, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado())
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            if (!permissoes.editar) {
                return@post call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para editar este contrato"))
            }

            val vinculo = body.copy(criadoPor = call.usuarioIdLogado())
            call.respond(HttpStatusCode.Created, VersaoContratoRepository.create(vinculo))
        }
    }
}
