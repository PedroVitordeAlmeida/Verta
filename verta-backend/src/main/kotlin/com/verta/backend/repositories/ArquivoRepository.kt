package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.ArquivoCreateDto
import com.verta.backend.dto.ArquivoDto
import com.verta.backend.models.Arquivos
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll

object ArquivoRepository {

    private fun ResultRow.toDto() = ArquivoDto(
        id = this[Arquivos.id],
        contratoId = this[Arquivos.contratoId],
        versaoId = this[Arquivos.versaoId],
        nomeArquivo = this[Arquivos.nomeArquivo],
        caminhoArquivo = this[Arquivos.caminhoArquivo],
        tipoArquivo = this[Arquivos.tipoArquivo],
        tamanho = this[Arquivos.tamanho],
        dataArmazenamento = this[Arquivos.dataArmazenamento]?.toString()
    )

    suspend fun findByContrato(contratoId: Int): List<ArquivoDto> = dbQuery {
        Arquivos.selectAll().where { Arquivos.contratoId eq contratoId }.map { it.toDto() }
    }

    suspend fun findByVersao(versaoId: Int): List<ArquivoDto> = dbQuery {
        Arquivos.selectAll().where { Arquivos.versaoId eq versaoId }.map { it.toDto() }
    }

    suspend fun findAll(): List<ArquivoDto> = dbQuery {
        Arquivos.selectAll().map { it.toDto() }
    }

    suspend fun findById(id: Int): ArquivoDto? = dbQuery {
        Arquivos.selectAll().where { Arquivos.id eq id }.map { it.toDto() }.singleOrNull()
    }

    suspend fun create(dto: ArquivoCreateDto): ArquivoDto = dbQuery {
        val insertedId = Arquivos.insert {
            it[contratoId] = dto.contratoId
            it[versaoId] = dto.versaoId
            it[nomeArquivo] = dto.nomeArquivo
            it[caminhoArquivo] = dto.caminhoArquivo
            it[tipoArquivo] = dto.tipoArquivo
            it[tamanho] = dto.tamanho
            it[dataArmazenamento] = CurrentDateTime
        } get Arquivos.id

        Arquivos.selectAll().where { Arquivos.id eq insertedId }.map { it.toDto() }.single()
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Arquivos.deleteWhere { Arquivos.id eq id } > 0
    }
}
