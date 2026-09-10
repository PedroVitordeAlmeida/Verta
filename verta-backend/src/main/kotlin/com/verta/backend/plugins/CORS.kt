package com.verta.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Libera o front (React/Figma-to-code da Verta) a consumir a API quando
 * rodarem em containers/portas diferentes dentro do docker-compose.
 */
fun Application.configureCORS() {
    val allowedHost = environment.config.property("cors.allowedHost").getString()

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowHost(allowedHost, schemes = listOf("http", "https"))
        // Em desenvolvimento local tambem libera localhost em qualquer porta comum
        allowHost("localhost:3000", schemes = listOf("http"))
        allowHost("localhost:5173", schemes = listOf("http"))

        allowCredentials = true
    }
}
