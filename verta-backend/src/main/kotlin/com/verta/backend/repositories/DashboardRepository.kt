package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.DashboardStatsDto
import com.verta.backend.models.Contratos
import com.verta.backend.models.StatusContrato
import com.verta.backend.models.Templates
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Alimenta os cards da tela Dashboard do Figma:
 * "Contratos ativos", "Aguardando assinatura", "Assinados este mes",
 * "Templates cadastrados".
 */
object DashboardRepository {

    suspend fun getStats(empresaId: Int? = null): DashboardStatsDto = dbQuery {
        val inicioDoMes = YearMonth.now().atDay(1).atStartOfDay()
        val fimDoMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59)

        fun empresaFilter(col: org.jetbrains.exposed.sql.Column<Int>) =
            if (empresaId != null) col eq empresaId else org.jetbrains.exposed.sql.Op.TRUE

        // "Ativos" = tudo que nao esta arquivado nem cancelado
        val contratosAtivos = Contratos
            .selectAll().where {
                (Contratos.status notInList listOf(StatusContrato.ARQUIVADO, StatusContrato.CANCELADO)) and
                    empresaFilter(Contratos.empresaId)
            }
            .count()

        // "Aguardando assinatura" = em revisao (equivalente mais proximo no schema atual)
        val aguardandoAssinatura = Contratos
            .selectAll().where { (Contratos.status eq StatusContrato.EM_REVISAO) and empresaFilter(Contratos.empresaId) }
            .count()

        val assinadosEsteMes = Contratos
            .selectAll().where {
                (Contratos.status eq StatusContrato.FINALIZADO) and
                    empresaFilter(Contratos.empresaId) and
                    (Contratos.dataAtualizacao.between(inicioDoMes, fimDoMes))
            }
            .count()

        val templatesCadastrados = Templates
            .selectAll().where { (Templates.ativo eq true) and empresaFilter(Templates.empresaId) }
            .count()

        DashboardStatsDto(
            contratosAtivos = contratosAtivos,
            aguardandoAssinatura = aguardandoAssinatura,
            assinadosEsteMes = assinadosEsteMes,
            templatesCadastrados = templatesCadastrados
        )
    }
}
