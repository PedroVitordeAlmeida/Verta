package com.verta.backend.plugins

import com.verta.backend.config.JwtConfig
import com.verta.backend.repositories.UsuarioRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/**
 * Configura a autenticacao JWT usada para proteger as rotas de negocio.
 *
 * Alem de validar assinatura/expiracao, confere se o claim "sessionToken" do token
 * ainda bate com o valor salvo em usuarios.token_sessao. Como cada login sobrescreve
 * esse valor, um token de uma sessao anterior (ex: login feito em outro dispositivo)
 * passa a ser rejeitado aqui - garantindo uma unica sessao ativa por usuario.
 */
fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier)
            validate { credential ->
                val usuarioId = credential.payload.getClaim("usuarioId").asInt()
                val sessionToken = credential.payload.getClaim("sessionToken").asString()

                if (usuarioId == null || sessionToken == null) {
                    return@validate null
                }

                val tokenAtivo = UsuarioRepository.findTokenSessao(usuarioId)
                if (tokenAtivo != null && tokenAtivo == sessionToken) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
