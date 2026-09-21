package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.ContratoCreateDto
import com.verta.backend.dto.ContratoDto
import com.verta.backend.dto.GerarContratoDto
import com.verta.backend.models.ContratoUsuarios
import com.verta.backend.models.Contratos
import com.verta.backend.models.StatusContrato
import com.verta.backend.models.Templates
import com.verta.backend.models.VersoesContrato
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

/**
 * Permissoes efetivas de um usuario sobre um contrato: dono/ADMIN tem tudo liberado,
 * caso contrario reflete o vinculo em contrato_usuarios (ou tudo negado se nao houver vinculo).
 */
data class ContratoPermissoes(
    val visualizar: Boolean,
    val editar: Boolean,
    val assinar: Boolean,
    val excluir: Boolean
)

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

    /**
     * Lista os contratos da empresa. Usuarios comuns so enxergam os que criaram ou
     * os que foram compartilhados com eles (contrato_usuarios.pode_visualizar); ADMIN ve tudo.
     */
    suspend fun findAll(empresaId: Int, status: String? = null, usuarioId: Int, isAdmin: Boolean): List<ContratoDto> = dbQuery {
        val conditions = mutableListOf<Op<Boolean>>()
        conditions.add(Contratos.empresaId eq empresaId)
        if (status != null) conditions.add(Contratos.status eq status)

        if (!isAdmin) {
            val idsCompartilhados = ContratoUsuarios
                .selectAll()
                .where { (ContratoUsuarios.usuarioId eq usuarioId) and (ContratoUsuarios.podeVisualizar eq true) }
                .map { it[ContratoUsuarios.contratoId] }

            conditions.add(
                if (idsCompartilhados.isEmpty()) {
                    Contratos.criadoPor eq usuarioId
                } else {
                    (Contratos.criadoPor eq usuarioId) or (Contratos.id inList idsCompartilhados)
                }
            )
        }

        Contratos.selectAll().where { conditions.reduce { acc, op -> acc and op } }
            .orderBy(Contratos.dataAtualizacao, SortOrder.DESC)
            .map { it.toDto() }
    }

    /** So retorna o contrato se ele pertencer a mesma empresa e o usuario puder visualiza-lo. */
    suspend fun findById(id: Int, empresaId: Int, usuarioId: Int, isAdmin: Boolean): ContratoDto? = dbQuery {
        val row = Contratos.selectAll().where { Contratos.id eq id }.singleOrNull() ?: return@dbQuery null
        if (row[Contratos.empresaId] != empresaId) return@dbQuery null
        if (isAdmin || row[Contratos.criadoPor] == usuarioId) return@dbQuery row.toDto()

        val compartilhado = ContratoUsuarios.selectAll().where {
            (ContratoUsuarios.contratoId eq id) and
                (ContratoUsuarios.usuarioId eq usuarioId) and
                (ContratoUsuarios.podeVisualizar eq true)
        }.count() > 0
        if (!compartilhado) return@dbQuery null
        row.toDto()
    }

    /** empresaId e criadoPor do contrato, para checagens de dono sem carregar o DTO inteiro. */
    suspend fun buscarDono(id: Int): Pair<Int, Int>? = dbQuery {
        Contratos.selectAll().where { Contratos.id eq id }.singleOrNull()
            ?.let { it[Contratos.empresaId] to it[Contratos.criadoPor] }
    }

    /** Permissoes efetivas do usuario sobre o contrato; null se o contrato nao existir na empresa informada. */
    suspend fun permissoesDe(id: Int, empresaId: Int, usuarioId: Int, isAdmin: Boolean): ContratoPermissoes? = dbQuery {
        val row = Contratos.selectAll().where { Contratos.id eq id }.singleOrNull() ?: return@dbQuery null
        if (row[Contratos.empresaId] != empresaId) return@dbQuery null
        if (isAdmin || row[Contratos.criadoPor] == usuarioId) {
            return@dbQuery ContratoPermissoes(visualizar = true, editar = true, assinar = true, excluir = true)
        }

        val vinculo = ContratoUsuarios.selectAll().where {
            (ContratoUsuarios.contratoId eq id) and (ContratoUsuarios.usuarioId eq usuarioId)
        }.singleOrNull() ?: return@dbQuery ContratoPermissoes(false, false, false, false)

        ContratoPermissoes(
            visualizar = vinculo[ContratoUsuarios.podeVisualizar],
            editar = vinculo[ContratoUsuarios.podeEditar],
            assinar = vinculo[ContratoUsuarios.podeAssinar],
            excluir = vinculo[ContratoUsuarios.podeExcluir]
        )
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
