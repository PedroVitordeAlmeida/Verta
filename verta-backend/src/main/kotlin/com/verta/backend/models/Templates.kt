package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.templates — modelos de contrato com variaveis {{campo}}. */
object Templates : Table("templates") {
    val id = integer("id").autoIncrement()
    val empresaId = integer("empresa_id").references(Empresas.id)
    val nome = varchar("nome", 150)
    val descricao = text("descricao").nullable()
    val conteudo = text("conteudo") // HTML/texto do template com placeholders
    val ativo = bool("ativo").default(true)
    val dataCriacao = datetime("data_criacao").nullable()
    // Validado pela equipe juridica da Verta (sempre true nos templates base do Plano Plus).
    val validadoJuridicamente = bool("validado_juridicamente").default(false)
    // true = um dos 5 templates base clonados ao atribuir um plano (nao conta na cota de
    // criacao de templates da empresa nem pode ser confundido com template criado por ela).
    val origemSistema = bool("origem_sistema").default(false)

    override val primaryKey = PrimaryKey(id)
}
