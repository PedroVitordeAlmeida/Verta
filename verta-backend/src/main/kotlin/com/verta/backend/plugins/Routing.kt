package com.verta.backend.plugins

import com.verta.backend.config.JwtConfig
import com.verta.backend.routes.arquivoRoutes
import com.verta.backend.routes.authRoutes
import com.verta.backend.routes.contratoRoutes
import com.verta.backend.routes.contratoUsuarioRoutes
import com.verta.backend.routes.dashboardRoutes
import com.verta.backend.routes.empresaRoutes
import com.verta.backend.routes.healthRoutes
import com.verta.backend.routes.planoRoutes
import com.verta.backend.routes.templateRoutes
import com.verta.backend.routes.usuarioRoutes
import com.verta.backend.routes.versaoContratoRoutes
import com.verta.backend.services.TemplateAiService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting(jwtConfig: JwtConfig, templateAiService: TemplateAiService) {
    routing {
        // Rotas publicas
        healthRoutes()
        authRoutes(jwtConfig)

        // Rotas de negocio - exigem token JWT (Authorization: Bearer <token>)
        authenticate("auth-jwt") {
            empresaRoutes()
            usuarioRoutes()
            planoRoutes()
            templateRoutes(templateAiService)
            contratoRoutes()
            contratoUsuarioRoutes()
            versaoContratoRoutes()
            arquivoRoutes()
            dashboardRoutes()
        }
    }
}
