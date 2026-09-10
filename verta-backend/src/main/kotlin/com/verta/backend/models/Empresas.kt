package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.empresas — empresas clientes que contratam a Verta. */
object Empresas : Table("empresas") {
    val id = integer("id").autoIncrement()
    val nome = varchar("nome", 150)
    val cnpj = varchar("cnpj", 18).nullable()
    val dataCadastro = datetime("data_cadastro").nullable()

    override val primaryKey = PrimaryKey(id)
}
