package com.verta.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/** Acesso as claims do usuario autenticado (ver JwtConfig.generateToken). */
fun ApplicationCall.usuarioIdLogado(): Int =
    principal<JWTPrincipal>()!!.payload.getClaim("usuarioId").asInt()

fun ApplicationCall.empresaIdLogado(): Int =
    principal<JWTPrincipal>()!!.payload.getClaim("empresaId").asInt()

fun ApplicationCall.perfilLogado(): String =
    principal<JWTPrincipal>()!!.payload.getClaim("perfil").asString()

fun ApplicationCall.isAdminLogado(): Boolean = perfilLogado() == "ADMIN"

/** SUPERADMIN = equipe Verta/Dalcomad; unico perfil autorizado a atribuir plano as empresas. */
fun ApplicationCall.isSuperAdminLogado(): Boolean = perfilLogado() == "SUPERADMIN"
