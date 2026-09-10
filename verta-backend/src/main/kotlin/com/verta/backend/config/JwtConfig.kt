package com.verta.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

/**
 * Geracao e configuracao dos tokens JWT usados para autenticar
 * usuarios (tabela public.usuarios) nas rotas protegidas.
 */
class JwtConfig(config: ApplicationConfig) {

    val secret: String = config.property("jwt.secret").getString()
    val issuer: String = config.property("jwt.issuer").getString()
    val audience: String = config.property("jwt.audience").getString()
    val realm: String = config.property("jwt.realm").getString()

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    /** Gera um token valido por 8 horas contendo id, empresaId e perfil do usuario. */
    fun generateToken(usuarioId: Int, empresaId: Int, perfil: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("usuarioId", usuarioId)
            .withClaim("empresaId", empresaId)
            .withClaim("perfil", perfil)
            .withExpiresAt(Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000))
            .sign(algorithm)
}
