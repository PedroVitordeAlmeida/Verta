package com.verta.backend.routes

import com.verta.backend.config.JwtConfig
import com.verta.backend.dto.ErrorResponseDto
import com.verta.backend.dto.LoginRequestDto
import com.verta.backend.dto.LoginResponseDto
import com.verta.backend.models.Usuarios
import com.verta.backend.repositories.UsuarioRepository
import com.verta.backend.security.PasswordUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

            val token = jwtConfig.generateToken(
                usuarioId = row[Usuarios.id],
                empresaId = row[Usuarios.empresaId],
                perfil = row[Usuarios.perfil]
            )

            call.respond(
                LoginResponseDto(
                    token = token,
                    usuarioId = row[Usuarios.id],
                    empresaId = row[Usuarios.empresaId],
                    nome = row[Usuarios.nome],
                    perfil = row[Usuarios.perfil]
                )
            )
        }
    }
}
