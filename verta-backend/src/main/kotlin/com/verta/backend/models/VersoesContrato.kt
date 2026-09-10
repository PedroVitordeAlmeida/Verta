package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.versoes_contrato — historico de versoes do conteudo do contrato. */
object VersoesContrato : Table("versoes_contrato") {
    val id = integer("id").autoIncrement()
    val contratoId = integer("contrato_id").references(Contratos.id)
    val numeroVersao = integer("numero_versao")
    val conteudo = text("conteudo")
    val criadoPor = integer("criado_por").references(Usuarios.id)
    val dataCriacao = datetime("data_criacao").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(contratoId, numeroVersao)
    }
}
