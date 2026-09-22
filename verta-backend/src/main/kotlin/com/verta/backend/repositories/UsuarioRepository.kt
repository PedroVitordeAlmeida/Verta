package com.verta.backend.repositories

import com.verta.backend.config.DatabaseFactory.dbQuery
import com.verta.backend.dto.UsuarioCreateDto
import com.verta.backend.dto.UsuarioDto
import com.verta.backend.dto.UsuarioUpdateDto
import com.verta.backend.models.Perfil
import com.verta.backend.models.Usuarios
import com.verta.backend.security.PasswordUtil
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object UsuarioRepository {

    private fun ResultRow.toDto() = UsuarioDto(
        id = this[Usuarios.id],
        empresaId = this[Usuarios.empresaId],
        nome = this[Usuarios.nome],
        email = this[Usuarios.email],
        perfil = this[Usuarios.perfil].name,
        ativo = this[Usuarios.ativo],
        dataCadastro = this[Usuarios.dataCadastro]?.toString()
    )

    suspend fun findAll(empresaId: Int? = null): List<UsuarioDto> = dbQuery {
        val query = if (empresaId != null) {
            Usuarios.selectAll().where { Usuarios.empresaId eq empresaId }
        } else {
            Usuarios.selectAll()
        }
        query.map { it.toDto() }
    }

    suspend fun findById(id: Int): UsuarioDto? = dbQuery {
        Usuarios.selectAll().where { Usuarios.id eq id }.map { it.toDto() }.singleOrNull()
    }

    /** Usado internamente pelo login - inclui o hash da senha. */
    suspend fun findRowByEmail(email: String): ResultRow? = dbQuery {
        Usuarios.selectAll().where { Usuarios.email eq email }.singleOrNull()
    }

    /** Grava o identificador da sessao ativa - login novo derruba a sessao anterior. */
    suspend fun updateTokenSessao(usuarioId: Int, tokenSessao: String?): Unit = dbQuery {
        Usuarios.update({ Usuarios.id eq usuarioId }) {
            it[Usuarios.tokenSessao] = tokenSessao
        }
        Unit
    }

    /** Usado pela validacao do JWT a cada requisicao (ver Security.kt). */
    suspend fun findTokenSessao(usuarioId: Int): String? = dbQuery {
        Usuarios.selectAll().where { Usuarios.id eq usuarioId }
            .map { it[Usuarios.tokenSessao] }
            .singleOrNull()
    }

    /** @throws LimitePlanoExcedidoException se a empresa ja atingiu o limite de usuarios do plano contratado. */
    suspend fun create(dto: UsuarioCreateDto): UsuarioDto = dbQuery {
        val limites = PlanoRepository.limitesDeNaTransacao(dto.empresaId)
        if (limites != null) {
            val atuais = Usuarios.selectAll().where { Usuarios.empresaId eq dto.empresaId }.count()
            if (atuais >= limites.maxUsuarios) {
                throw LimitePlanoExcedidoException(
                    "Limite de ${limites.maxUsuarios} usuários do plano atingido. Remova um usuário existente ou peça upgrade de plano."
                )
            }
        }

        val insertedId = Usuarios.insert {
            it[empresaId] = dto.empresaId
            it[nome] = dto.nome
            it[email] = dto.email
            it[senha] = PasswordUtil.hash(dto.senha)
            it[perfil] = Perfil.fromInput(dto.perfil)
            it[ativo] = dto.ativo
            it[dataCadastro] = CurrentDateTime
        } get Usuarios.id

        Usuarios.selectAll().where { Usuarios.id eq insertedId }.map { it.toDto() }.single()
    }

    suspend fun update(id: Int, dto: UsuarioUpdateDto): Boolean = dbQuery {
        Usuarios.update({ Usuarios.id eq id }) {
            it[nome] = dto.nome
            it[email] = dto.email
            it[perfil] = Perfil.fromInput(dto.perfil)
            it[ativo] = dto.ativo
            if (!dto.senha.isNullOrBlank()) {
                it[senha] = PasswordUtil.hash(dto.senha)
            }
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Usuarios.deleteWhere { Usuarios.id eq id } > 0
    }
}
