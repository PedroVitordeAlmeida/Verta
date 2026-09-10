package com.verta.backend.plugins

import com.verta.backend.config.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/** Configura a autenticacao JWT usada para proteger as rotas de negocio. */
fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("usuarioId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
