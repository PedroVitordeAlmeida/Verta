package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.TemplateCreateDto
import com.verta.backend.dto.TemplateDto
import com.verta.backend.models.Contratos
import com.verta.backend.models.Templates
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object TemplateRepository {

    private fun ResultRow.toDto() = TemplateDto(
        id = this[Templates.id],
        empresaId = this[Templates.empresaId],
        nome = this[Templates.nome],
        descricao = this[Templates.descricao],
        conteudo = this[Templates.conteudo],
        ativo = this[Templates.ativo],
        dataCriacao = this[Templates.dataCriacao]?.toString()
    )

    suspend fun findAll(empresaId: Int? = null): List<TemplateDto> = dbQuery {
        val query = if (empresaId != null) {
            Templates.selectAll().where { Templates.empresaId eq empresaId }
        } else {
            Templates.selectAll()
        }
        query.map { it.toDto() }
    }

    suspend fun findById(id: Int): TemplateDto? = dbQuery {
        Templates.selectAll().where { Templates.id eq id }.map { it.toDto() }.singleOrNull()
    }

    suspend fun create(dto: TemplateCreateDto): TemplateDto = dbQuery {
        val insertedId = Templates.insert {
            it[empresaId] = dto.empresaId
            it[nome] = dto.nome
            it[descricao] = dto.descricao
            it[conteudo] = dto.conteudo
            it[ativo] = dto.ativo
            it[dataCriacao] = CurrentDateTime
        } get Templates.id

        Templates.selectAll().where { Templates.id eq insertedId }.map { it.toDto() }.single()
    }

    suspend fun update(id: Int, dto: TemplateCreateDto): Boolean = dbQuery {
        Templates.update({ Templates.id eq id }) {
            it[nome] = dto.nome
            it[descricao] = dto.descricao
            it[conteudo] = dto.conteudo
            it[ativo] = dto.ativo
        } > 0
    }

    /**
     * Exclui o template. Contratos ja gerados a partir dele nao sao apagados (perderiam
     * historico) - so tem a referencia desvinculada, senao o DELETE falha com violacao de
     * FK sempre que o template ja tiver sido usado, que era o caso que nao estava excluindo.
     */
    suspend fun delete(id: Int): Boolean = dbQuery {
        Contratos.update({ Contratos.templateId eq id }) {
            it[templateId] = null
        }
        Templates.deleteWhere { Templates.id eq id } > 0
    }
}
