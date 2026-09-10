package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.contratos — contratos gerados a partir de um template. */
object Contratos : Table("contratos") {
    val id = integer("id").autoIncrement()
    val empresaId = integer("empresa_id").references(Empresas.id)
    val templateId = integer("template_id").references(Templates.id).nullable()
    val titulo = varchar("titulo", 200)
    val tipo = varchar("tipo", 100).nullable()
    // valores usados nas telas do Figma: RASCUNHO, EM_ELABORACAO,
    // AGUARDANDO_ASSINATURA, ASSINADO, ARQUIVADO
    val status = varchar("status", 50).default("RASCUNHO")
    val criadoPor = integer("criado_por").references(Usuarios.id)
    val dataCriacao = datetime("data_criacao").nullable()
    val dataAtualizacao = datetime("data_atualizacao").nullable()

    override val primaryKey = PrimaryKey(id)
}
