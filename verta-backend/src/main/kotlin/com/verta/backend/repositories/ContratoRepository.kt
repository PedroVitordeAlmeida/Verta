package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.ContratoCreateDto
import com.verta.backend.dto.ContratoDto
import com.verta.backend.dto.GerarContratoDto
import com.verta.backend.models.Contratos
import com.verta.backend.models.StatusContrato
import com.verta.backend.models.Templates
import com.verta.backend.models.VersoesContrato
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object ContratoRepository {

    private fun ResultRow.toDto() = ContratoDto(
        id = this[Contratos.id],
        empresaId = this[Contratos.empresaId],
        templateId = this[Contratos.templateId],
        titulo = this[Contratos.titulo],
        tipo = this[Contratos.tipo],
        status = this[Contratos.status],
        criadoPor = this[Contratos.criadoPor],
        dataCriacao = this[Contratos.dataCriacao]?.toString(),
        dataAtualizacao = this[Contratos.dataAtualizacao]?.toString()
    )

    suspend fun findAll(empresaId: Int? = null, status: String? = null): List<ContratoDto> = dbQuery {
        val conditions = mutableListOf<org.jetbrains.exposed.sql.Op<Boolean>>()
        if (empresaId != null) conditions.add(Contratos.empresaId eq empresaId)
        if (status != null) conditions.add(Contratos.status eq status)

        val query = if (conditions.isEmpty()) {
            Contratos.selectAll()
        } else {
            Contratos.selectAll().where { conditions.reduce { acc, op -> acc and op } }
        }
        query.orderBy(Contratos.dataAtualizacao, SortOrder.DESC).map { it.toDto() }
    }

    suspend fun findById(id: Int): ContratoDto? = dbQuery {
        Contratos.selectAll().where { Contratos.id eq id }.map { it.toDto() }.singleOrNull()
    }

    suspend fun create(dto: ContratoCreateDto): ContratoDto = dbQuery {
        val insertedId = Contratos.insert {
            it[empresaId] = dto.empresaId
            it[templateId] = dto.templateId
            it[titulo] = dto.titulo
            it[tipo] = dto.tipo
            it[status] = StatusContrato.RASCUNHO
            it[criadoPor] = dto.criadoPor
            it[dataCriacao] = CurrentDateTime
            it[dataAtualizacao] = CurrentDateTime
        } get Contratos.id

        Contratos.selectAll().where { Contratos.id eq insertedId }.map { it.toDto() }.single()
    }

    /** @throws IllegalArgumentException se o status nao for um dos valores aceitos pelo banco. */
    suspend fun updateStatus(id: Int, status: String): Boolean {
        require(StatusContrato.isValido(status)) {
            "Status invalido. Valores aceitos: ${StatusContrato.VALORES_VALIDOS.joinToString()}"
        }
        return dbQuery {
            Contratos.update({ Contratos.id eq id }) {
                it[Contratos.status] = status
                it[dataAtualizacao] = CurrentDateTime
            } > 0
        }
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Contratos.deleteWhere { Contratos.id eq id } > 0
    }

    /**
     * Usado pela tela "Geracao de Contrato": busca o template, substitui as
     * variaveis {{campo}} pelos valores informados e cria o contrato ja com
     * a sua primeira versao (versoes_contrato, numero_versao = 1).
     */
    suspend fun generateFromTemplate(dto: GerarContratoDto): ContratoDto? = dbQuery {
        val templateRow = Templates.selectAll().where { Templates.id eq dto.templateId }.singleOrNull()
            ?: return@dbQuery null

        var conteudoFinal = templateRow[Templates.conteudo]
        dto.valores.forEach { (chave, valor) ->
            conteudoFinal = conteudoFinal.replace("{{$chave}}", valor)
        }

        val contratoId = Contratos.insert {
            it[empresaId] = dto.empresaId
            it[templateId] = dto.templateId
            it[titulo] = dto.titulo
            it[tipo] = dto.tipo
            it[status] = StatusContrato.RASCUNHO
            it[criadoPor] = dto.criadoPor
            it[dataCriacao] = CurrentDateTime
            it[dataAtualizacao] = CurrentDateTime
        } get Contratos.id

        VersoesContrato.insert {
            it[VersoesContrato.contratoId] = contratoId
            it[numeroVersao] = 1
            it[conteudo] = conteudoFinal
            it[criadoPor] = dto.criadoPor
            it[dataCriacao] = CurrentDateTime
        }

        Contratos.selectAll().where { Contratos.id eq contratoId }.map { it.toDto() }.single()
    }
}
