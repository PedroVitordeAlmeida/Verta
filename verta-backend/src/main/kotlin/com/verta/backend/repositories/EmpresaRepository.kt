package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.EmpresaCreateDto
import com.verta.backend.dto.EmpresaDto
import com.verta.backend.models.Empresas
import com.verta.backend.models.PeriodoPlano
import com.verta.backend.models.Planos
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

object EmpresaRepository {

    private fun ResultRow.toDto() = EmpresaDto(
        id = this[Empresas.id],
        nome = this[Empresas.nome],
        cnpj = this[Empresas.cnpj],
        dataCadastro = this[Empresas.dataCadastro]?.toString(),
        planoId = this[Empresas.planoId],
        periodoPlano = this[Empresas.periodoPlano],
        dataInicioPlano = this[Empresas.dataInicioPlano]?.toString(),
        dataFimPlano = this[Empresas.dataFimPlano]?.toString()
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

    /**
     * Atribui (ou troca) o plano de uma empresa: grava plano/periodo/vigencia e clona/sincroniza
     * os 5 templates base ([TemplateRepository.sincronizarTemplatesBaseNaTransacao]) com a validacao
     * juridica do novo plano. Retorna null se a empresa nao existe.
     *
     * @throws IllegalArgumentException se o plano ou o periodo informados forem invalidos.
     */
    suspend fun atribuirPlano(empresaId: Int, planoId: Int, periodo: String): EmpresaDto? = dbQuery {
        val empresaExiste = Empresas.selectAll().where { Empresas.id eq empresaId }.count() > 0
        if (!empresaExiste) return@dbQuery null

        val periodoEnum = PeriodoPlano.fromInput(periodo)
        val planoRow = Planos.selectAll().where { Planos.id eq planoId }.singleOrNull()
            ?: throw IllegalArgumentException("Plano nao encontrado")

        val inicio = LocalDateTime.now()
        val fim = inicio.plusMonths(periodoEnum.meses)

        Empresas.update({ Empresas.id eq empresaId }) {
            it[Empresas.planoId] = planoId
            it[Empresas.periodoPlano] = periodoEnum.name
            it[Empresas.dataInicioPlano] = inicio
            it[Empresas.dataFimPlano] = fim
        }

        TemplateRepository.sincronizarTemplatesBaseNaTransacao(empresaId, planoRow[Planos.validacaoJuridicaBase])

        Empresas.selectAll().where { Empresas.id eq empresaId }.map { it.toDto() }.single()
    }
}
