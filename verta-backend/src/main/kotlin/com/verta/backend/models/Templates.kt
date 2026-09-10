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

    override val primaryKey = PrimaryKey(id)
}
