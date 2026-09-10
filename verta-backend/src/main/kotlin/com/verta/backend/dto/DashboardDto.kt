package com.verta.backend.dto

import kotlinx.serialization.Serializable

// Numeros exibidos nos cards do Dashboard (tela Verta - Dashboard).
@Serializable
data class DashboardStatsDto(
    val contratosAtivos: Long,
    val aguardandoAssinatura: Long,
    val assinadosEsteMes: Long,
    val templatesCadastrados: Long
)
