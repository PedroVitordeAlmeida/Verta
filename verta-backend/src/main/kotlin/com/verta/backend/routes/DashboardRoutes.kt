package com.verta.backend.routes

import com.verta.backend.repositories.DashboardRepository
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

// Alimenta os 4 cards da tela Dashboard.
fun Route.dashboardRoutes() {
    get("/dashboard/stats") {
        val empresaId = call.request.queryParameters["empresaId"]?.toIntOrNull()
        call.respond(DashboardRepository.getStats(empresaId))
    }
}
