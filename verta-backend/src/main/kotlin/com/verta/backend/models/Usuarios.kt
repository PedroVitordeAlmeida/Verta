package com.verta.backend.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Espelha public.usuarios — usuarios de cada empresa cliente. */
object Usuarios : Table("usuarios") {
    val id = integer("id").autoIncrement()
    val empresaId = integer("empresa_id").references(Empresas.id)
    val nome = varchar("nome", 150)
    val email = varchar("email", 150).uniqueIndex()
    val senha = varchar("senha", 255) // hash bcrypt, nunca texto puro
    val perfil = varchar("perfil", 50) // ex: ADMINISTRADOR, EDITOR, VISUALIZADOR
    val ativo = bool("ativo").default(true)
    val dataCadastro = datetime("data_cadastro").nullable()

    override val primaryKey = PrimaryKey(id)
}
