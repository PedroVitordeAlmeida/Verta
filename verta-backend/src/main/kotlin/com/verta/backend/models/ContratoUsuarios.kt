package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Espelha public.contrato_usuarios — tabela nova (veio no VERTA_backup.sql)
 * que define quais usuarios podem ver/editar/assinar/excluir cada contrato.
 */
object ContratoUsuarios : Table("contrato_usuarios") {
    val id = integer("id").autoIncrement()
    val contratoId = integer("contrato_id").references(Contratos.id)
    val usuarioId = integer("usuario_id").references(Usuarios.id)
    val podeVisualizar = bool("pode_visualizar").default(true)
    val podeEditar = bool("pode_editar").default(false)
    val podeAssinar = bool("pode_assinar").default(false)
    val podeExcluir = bool("pode_excluir").default(false)
    val dataVinculo = datetime("data_vinculo").nullable()
    val empresaId = integer("empresa_id").references(Empresas.id).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(contratoId, usuarioId)
    }
}
