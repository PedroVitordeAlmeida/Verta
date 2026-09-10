package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.ContratoUsuarioCreateDto
import com.verta.backend.dto.ContratoUsuarioDto
import com.verta.backend.dto.ContratoUsuarioPermissoesUpdateDto
import com.verta.backend.models.ContratoUsuarios
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.update

object ContratoUsuarioRepository {

    private fun ResultRow.toDto() = ContratoUsuarioDto(
        id = this[ContratoUsuarios.id],
        contratoId = this[ContratoUsuarios.contratoId],
        usuarioId = this[ContratoUsuarios.usuarioId],
        podeVisualizar = this[ContratoUsuarios.podeVisualizar],
        podeEditar = this[ContratoUsuarios.podeEditar],
        podeAssinar = this[ContratoUsuarios.podeAssinar],
        podeExcluir = this[ContratoUsuarios.podeExcluir],
        dataVinculo = this[ContratoUsuarios.dataVinculo]?.toString(),
        empresaId = this[ContratoUsuarios.empresaId]
    )

    suspend fun findByContrato(contratoId: Int): List<ContratoUsuarioDto> = dbQuery {
        ContratoUsuarios.selectAll().where { ContratoUsuarios.contratoId eq contratoId }.map { it.toDto() }
    }

    suspend fun findByUsuario(usuarioId: Int): List<ContratoUsuarioDto> = dbQuery {
        ContratoUsuarios.selectAll().where { ContratoUsuarios.usuarioId eq usuarioId }.map { it.toDto() }
    }

    suspend fun create(dto: ContratoUsuarioCreateDto): ContratoUsuarioDto = dbQuery {
        val insertedId = ContratoUsuarios.insert {
            it[contratoId] = dto.contratoId
            it[usuarioId] = dto.usuarioId
            it[empresaId] = dto.empresaId
            it[podeVisualizar] = dto.podeVisualizar
            it[podeEditar] = dto.podeEditar
            it[podeAssinar] = dto.podeAssinar
            it[podeExcluir] = dto.podeExcluir
            it[dataVinculo] = CurrentDateTime
        } get ContratoUsuarios.id

        ContratoUsuarios.selectAll().where { ContratoUsuarios.id eq insertedId }.map { it.toDto() }.single()
    }

    suspend fun updatePermissoes(id: Int, dto: ContratoUsuarioPermissoesUpdateDto): Boolean = dbQuery {
        ContratoUsuarios.update({ ContratoUsuarios.id eq id }) {
            it[podeVisualizar] = dto.podeVisualizar
            it[podeEditar] = dto.podeEditar
            it[podeAssinar] = dto.podeAssinar
            it[podeExcluir] = dto.podeExcluir
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        ContratoUsuarios.deleteWhere { ContratoUsuarios.id eq id } > 0
    }
}
