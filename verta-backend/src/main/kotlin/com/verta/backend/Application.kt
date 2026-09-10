package com.verta.backend

import com.verta.backend.config.DatabaseFactory
import com.verta.backend.config.JwtConfig
import com.verta.backend.plugins.configureCORS
import com.verta.backend.plugins.configureRouting
import com.verta.backend.plugins.configureSecurity
import com.verta.backend.plugins.configureSerialization
import com.verta.backend.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    val jwtConfig = JwtConfig(environment.config)

    configureSerialization()
    configureStatusPages()
    configureCORS()
    configureSecurity(jwtConfig)
    configureRouting(jwtConfig)
}
