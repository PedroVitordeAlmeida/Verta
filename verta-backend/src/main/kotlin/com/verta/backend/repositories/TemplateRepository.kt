package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.data.TemplatesBaseSeed
import com.verta.backend.dto.TemplateCreateDto
import com.verta.backend.dto.TemplateDto
import com.verta.backend.models.Contratos
import com.verta.backend.models.Templates
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
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
        dataCriacao = this[Templates.dataCriacao]?.toString(),
        validadoJuridicamente = this[Templates.validadoJuridicamente],
        origemSistema = this[Templates.origemSistema]
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

    /** @throws LimitePlanoExcedidoException se a empresa ja atingiu o limite de templates do plano contratado. */
    suspend fun create(dto: TemplateCreateDto): TemplateDto = dbQuery {
        val limites = PlanoRepository.limitesDeNaTransacao(dto.empresaId)
        if (limites != null) {
            val atuais = Templates
                .selectAll()
                .where { (Templates.empresaId eq dto.empresaId) and (Templates.origemSistema eq false) }
                .count()
            if (atuais >= limites.maxTemplates) {
                throw LimitePlanoExcedidoException(
                    "Limite de ${limites.maxTemplates} templates do plano atingido. Exclua um template existente ou peça upgrade de plano."
                )
            }
        }

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

    /**
     * Clona os 5 templates base (ver [TemplatesBaseSeed]) para a empresa na primeira vez que
     * ela recebe um plano; em atribuicoes seguintes (renovacao ou troca de plano) so atualiza a
     * flag [Templates.validadoJuridicamente] das copias ja existentes, sem duplicar linhas.
     *
     * So chame de dentro de uma transacao ja aberta (dbQuery de outro repositorio) - nao abre
     * uma nova. Copias base nao contam na cota de criacao de templates da empresa
     * ([Templates.origemSistema] = true, ver [create]).
     */
    fun sincronizarTemplatesBaseNaTransacao(empresaId: Int, validadoJuridicamente: Boolean) {
        val jaClonados = Templates
            .selectAll()
            .where { (Templates.empresaId eq empresaId) and (Templates.origemSistema eq true) }
            .count() > 0

        if (jaClonados) {
            Templates.update({ (Templates.empresaId eq empresaId) and (Templates.origemSistema eq true) }) {
                it[Templates.validadoJuridicamente] = validadoJuridicamente
            }
            return
        }

        TemplatesBaseSeed.TEMPLATES.forEach { base ->
            Templates.insert {
                it[Templates.empresaId] = empresaId
                it[nome] = base.nome
                it[descricao] = base.descricao
                it[conteudo] = base.conteudo
                it[ativo] = true
                it[dataCriacao] = CurrentDateTime
                it[Templates.validadoJuridicamente] = validadoJuridicamente
                it[origemSistema] = true
            }
        }
    }
}
