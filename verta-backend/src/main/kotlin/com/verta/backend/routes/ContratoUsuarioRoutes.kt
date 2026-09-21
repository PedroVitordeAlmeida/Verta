package com.verta.backend.routes

import com.verta.backend.dto.ContratoUsuarioCreateDto
import com.verta.backend.dto.ContratoUsuarioPermissoesUpdateDto
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.plugins.empresaIdLogado
import com.verta.backend.plugins.isAdminLogado
import com.verta.backend.plugins.usuarioIdLogado
import com.verta.backend.repositories.ContratoRepository
import com.verta.backend.repositories.ContratoUsuarioRepository
import com.verta.backend.repositories.UsuarioRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Controla quem pode ver/editar/assinar/excluir cada contrato. Compartilhar (criar/editar/
 * remover vinculo) e restrito ao criador do contrato ou a um ADMIN da empresa - quem recebeu
 * o compartilhamento nao pode repassa-lo adiante, mesmo com todas as permissoes concedidas.
 */
fun Route.contratoUsuarioRoutes() {
    route("/contrato-usuarios") {
        get {
            val contratoId = call.request.queryParameters["contratoId"]?.toIntOrNull()
            val usuarioId = call.request.queryParameters["usuarioId"]?.toIntOrNull()
            when {
                contratoId != null -> {
                    val permissoes = ContratoRepository.permissoesDe(
                        contratoId, call.empresaIdLogado(), call.usuarioIdLogado(), call.isAdminLogado()
                    ) ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
                    if (!permissoes.visualizar) {
                        return@get call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para ver os compartilhamentos deste contrato"))
                    }
                    call.respond(ContratoUsuarioRepository.findByContrato(contratoId))
                }
                usuarioId != null -> {
                    if (usuarioId != call.usuarioIdLogado() && !call.isAdminLogado()) {
                        return@get call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao"))
                    }
                    call.respond(ContratoUsuarioRepository.findByUsuario(usuarioId))
                }
                else -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponseDto("Informe contratoId ou usuarioId na query string")
                )
            }
        }

        post {
            val body = call.receive<ContratoUsuarioCreateDto>()

            val (contratoEmpresaId, criadoPor) = ContratoRepository.buscarDono(body.contratoId)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            if (contratoEmpresaId != call.empresaIdLogado()) {
                return@post call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Contrato nao encontrado"))
            }
            if (!call.isAdminLogado() && criadoPor != call.usuarioIdLogado()) {
                return@post call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas o criador do contrato ou um administrador pode compartilha-lo"))
            }

            UsuarioRepository.findById(body.usuarioId)
                ?.takeIf { it.empresaId == call.empresaIdLogado() }
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("Usuario invalido"))

            val vinculo = body.copy(empresaId = call.empresaIdLogado())
            call.respond(HttpStatusCode.Created, ContratoUsuarioRepository.create(vinculo))
        }

        put("/{id}/permissoes") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val vinculo = ContratoUsuarioRepository.findById(id)
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))
            val (contratoEmpresaId, criadoPor) = ContratoRepository.buscarDono(vinculo.contratoId)
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))
            if (contratoEmpresaId != call.empresaIdLogado() ||
                (!call.isAdminLogado() && criadoPor != call.usuarioIdLogado())
            ) {
                return@put call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Apenas o criador do contrato ou um administrador pode alterar as permissoes"))
            }

            val body = call.receive<ContratoUsuarioPermissoesUpdateDto>()
            ContratoUsuarioRepository.updatePermissoes(id, body)
            call.respond(MessageResponseDto("Permissoes atualizadas com sucesso"))
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponseDto("id invalido"))

            val vinculo = ContratoUsuarioRepository.findById(id)
                ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))
            val (contratoEmpresaId, criadoPor) = ContratoRepository.buscarDono(vinculo.contratoId)
                ?: return@delete call.respond(HttpStatusCode.NotFound, ErrorResponseDto("Vinculo nao encontrado"))

            // O criador do contrato, um ADMIN, ou o proprio usuario compartilhado (para sair do
            // contrato) podem remover o vinculo.
            val podeRemover = contratoEmpresaId == call.empresaIdLogado() &&
                (call.isAdminLogado() || criadoPor == call.usuarioIdLogado() || vinculo.usuarioId == call.usuarioIdLogado())
            if (!podeRemover) {
                return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponseDto("Sem permissao para remover este vinculo"))
            }

            ContratoUsuarioRepository.delete(id)
            call.respond(MessageResponseDto("Vinculo removido com sucesso"))
        }
    }
}
