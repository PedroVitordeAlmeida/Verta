package com.verta.backend.routes

import com.verta.backend.config.JwtConfig
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.LoginRequestDto
import com.verta.backend.dto.LoginResponseDto
import com.verta.backend.dto.MessageResponseDto
import com.verta.backend.models.Usuarios
import com.verta.backend.repositories.UsuarioRepository
import com.verta.backend.security.PasswordUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.authRoutes(jwtConfig: JwtConfig) {
    route("/auth") {
        post("/login") {
            val body = call.receive<LoginRequestDto>()
            val row = UsuarioRepository.findRowByEmail(body.email)

            if (row == null || !row[Usuarios.ativo]) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto("Credenciais invalidas"))
                return@post
            }

            val senhaOk = PasswordUtil.verify(body.senha, row[Usuarios.senha])
            if (!senhaOk) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponseDto("Credenciais invalidas"))
                return@post
            }

            val usuarioId = row[Usuarios.id]

            // Cada login gera um identificador de sessao novo e sobrescreve o anterior no
            // banco, o que derruba automaticamente qualquer sessao ja ativa desse usuario
            // (a proxima requisicao dela vai falhar a validacao em Security.kt).
            val sessionToken = UUID.randomUUID().toString()
            UsuarioRepository.updateTokenSessao(usuarioId, sessionToken)

            val token = jwtConfig.generateToken(
                usuarioId = usuarioId,
                empresaId = row[Usuarios.empresaId],
                perfil = row[Usuarios.perfil],
                sessionToken = sessionToken
            )

            call.respond(
                LoginResponseDto(
                    token = token,
                    usuarioId = usuarioId,
                    empresaId = row[Usuarios.empresaId],
                    nome = row[Usuarios.nome],
                    perfil = row[Usuarios.perfil]
                )
            )
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val usuarioId = call.principal<JWTPrincipal>()!!.payload.getClaim("usuarioId").asInt()
                UsuarioRepository.updateTokenSessao(usuarioId, null)
                call.respond(MessageResponseDto("Sessao encerrada"))
            }
        }
    }
}
