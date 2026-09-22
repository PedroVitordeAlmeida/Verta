package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.PlanoDto
import com.verta.backend.models.Empresas
import com.verta.backend.models.Planos
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

/** Empresa tentou passar do limite (usuarios/templates) do plano contratado. */
class LimitePlanoExcedidoException(message: String) : RuntimeException(message)

/** Limites efetivos do plano de uma empresa; null em qualquer campo de [PlanoRepository.limitesDe] = sem plano atribuido (sem limite). */
data class PlanoLimites(
    val maxUsuarios: Int,
    val maxTemplates: Int,
    val validacaoJuridicaBase: Boolean
)

object PlanoRepository {

    private fun ResultRow.toDto() = PlanoDto(
        id = this[Planos.id],
        nome = this[Planos.nome],
        maxUsuarios = this[Planos.maxUsuarios],
        maxTemplates = this[Planos.maxTemplates],
        validacaoJuridicaBase = this[Planos.validacaoJuridicaBase],
        quotaGeracaoJuridicaMensal = this[Planos.quotaGeracaoJuridicaMensal]
    )

    suspend fun findAll(): List<PlanoDto> = dbQuery {
        Planos.selectAll().map { it.toDto() }
    }

    suspend fun findById(id: Int): PlanoDto? = dbQuery {
        Planos.selectAll().where { Planos.id eq id }.map { it.toDto() }.singleOrNull()
    }

    /**
     * So chame de dentro de uma transacao ja aberta (dbQuery de outro repositorio) - nao abre
     * uma nova, so consulta na transacao corrente. Retorna null se a empresa nao existe ou
     * nao tem plano atribuido (nesse caso nao ha limite nenhum aplicado).
     */
    fun limitesDeNaTransacao(empresaId: Int): PlanoLimites? {
        val empresaRow = Empresas.selectAll().where { Empresas.id eq empresaId }.singleOrNull() ?: return null
        val planoId = empresaRow[Empresas.planoId] ?: return null
        val planoRow = Planos.selectAll().where { Planos.id eq planoId }.singleOrNull() ?: return null
        return PlanoLimites(
            maxUsuarios = planoRow[Planos.maxUsuarios],
            maxTemplates = planoRow[Planos.maxTemplates],
            validacaoJuridicaBase = planoRow[Planos.validacaoJuridicaBase]
        )
    }
}
