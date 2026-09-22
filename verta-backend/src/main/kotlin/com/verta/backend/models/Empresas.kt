package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.empresas — empresas clientes que contratam a Verta. */
object Empresas : Table("empresas") {
    val id = integer("id").autoIncrement()
    val nome = varchar("nome", 150)
    val cnpj = varchar("cnpj", 18).nullable()
    val dataCadastro = datetime("data_cadastro").nullable()

    // Assinatura ativa da empresa (null = nenhum plano atribuido ainda, sem limites aplicados).
    val planoId = integer("plano_id").references(Planos.id).nullable()
    val periodoPlano = varchar("periodo_plano", 20).nullable()
    val dataInicioPlano = datetime("data_inicio_plano").nullable()
    val dataFimPlano = datetime("data_fim_plano").nullable()

    override val primaryKey = PrimaryKey(id)
}
