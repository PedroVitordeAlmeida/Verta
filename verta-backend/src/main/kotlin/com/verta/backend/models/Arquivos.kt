package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Espelha public.arquivos — arquivos (PDF/DOCX) anexados a uma VERSAO
 * especifica de um contrato (schema atualizado a partir do backup
 * VERTA_backup.sql: agora existe a coluna versao_id).
 */
object Arquivos : Table("arquivos") {
    val id = integer("id").autoIncrement()
    val contratoId = integer("contrato_id").references(Contratos.id)
    val nomeArquivo = varchar("nome_arquivo", 255)
    val caminhoArquivo = varchar("caminho_arquivo", 500)
    val tipoArquivo = varchar("tipo_arquivo", 100).nullable()
    val tamanho = long("tamanho").nullable()
    val dataArmazenamento = datetime("data_armazenamento").nullable()
    val versaoId = integer("versao_id").references(VersoesContrato.id)

    override val primaryKey = PrimaryKey(id)
}
