package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.EmpresaCreateDto
import com.verta.backend.dto.EmpresaDto
import com.verta.backend.models.Empresas
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object EmpresaRepository {

    private fun ResultRow.toDto() = EmpresaDto(
        id = this[Empresas.id],
        nome = this[Empresas.nome],
        cnpj = this[Empresas.cnpj],
        dataCadastro = this[Empresas.dataCadastro]?.toString()
    )

    suspend fun findAll(): List<EmpresaDto> = dbQuery {
        Empresas.selectAll().map { it.toDto() }
    }

    suspend fun findById(id: Int): EmpresaDto? = dbQuery {
        Empresas.selectAll().where { Empresas.id eq id }.map { it.toDto() }.singleOrNull()
    }

    suspend fun create(dto: EmpresaCreateDto): EmpresaDto = dbQuery {
        val insertedId = Empresas.insert {
            it[nome] = dto.nome
            it[cnpj] = dto.cnpj
            it[dataCadastro] = CurrentDateTime
        } get Empresas.id

        Empresas.selectAll().where { Empresas.id eq insertedId }.map { it.toDto() }.single()
    }

    suspend fun update(id: Int, dto: EmpresaCreateDto): Boolean = dbQuery {
        Empresas.update({ Empresas.id eq id }) {
            it[nome] = dto.nome
            it[cnpj] = dto.cnpj
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Empresas.deleteWhere { Empresas.id eq id } > 0
    }
}
