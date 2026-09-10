package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.VersaoContratoCreateDto
import com.verta.backend.dto.VersaoContratoDto
import com.verta.backend.models.VersoesContrato
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll

object VersaoContratoRepository {

    private fun ResultRow.toDto() = VersaoContratoDto(
        id = this[VersoesContrato.id],
        contratoId = this[VersoesContrato.contratoId],
        numeroVersao = this[VersoesContrato.numeroVersao],
        conteudo = this[VersoesContrato.conteudo],
        criadoPor = this[VersoesContrato.criadoPor],
        dataCriacao = this[VersoesContrato.dataCriacao]?.toString()
    )

    suspend fun findByContrato(contratoId: Int): List<VersaoContratoDto> = dbQuery {
        VersoesContrato.selectAll().where { VersoesContrato.contratoId eq contratoId }
            .orderBy(VersoesContrato.numeroVersao, SortOrder.DESC)
            .map { it.toDto() }
    }

    suspend fun findAll(): List<VersaoContratoDto> = dbQuery {
        VersoesContrato.selectAll().map { it.toDto() }
    }

    /** Cria uma nova versao incrementando o numero_versao automaticamente. */
    suspend fun create(dto: VersaoContratoCreateDto): VersaoContratoDto = dbQuery {
        val ultimaVersao = VersoesContrato
            .selectAll().where { VersoesContrato.contratoId eq dto.contratoId }
            .orderBy(VersoesContrato.numeroVersao, SortOrder.DESC)
            .limit(1)
            .map { it[VersoesContrato.numeroVersao] }
            .singleOrNull() ?: 0

        val insertedId = VersoesContrato.insert {
            it[contratoId] = dto.contratoId
            it[numeroVersao] = ultimaVersao + 1
            it[conteudo] = dto.conteudo
            it[criadoPor] = dto.criadoPor
            it[dataCriacao] = CurrentDateTime
        } get VersoesContrato.id

        VersoesContrato.selectAll().where { VersoesContrato.id eq insertedId }.map { it.toDto() }.single()
    }
}
