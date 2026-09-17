package com.verta.backend.models

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

/**
 * O driver JDBC do Postgres precisa saber explicitamente o nome do tipo ENUM nativo
 * (ex: tipo_perfil) ao enviar o valor, senao o Postgres rejeita o INSERT/UPDATE com
 * "column is of type X but expression is of type character varying".
 */
@PublishedApi
internal class PGEnum<T : Enum<T>>(postgresTypeName: String, enumValue: T?) : PGobject() {
    init {
        type = postgresTypeName
        value = enumValue?.name
    }
}

/** Coluna para um ENUM nativo do Postgres (ex: tipo_perfil), mapeada para um enum Kotlin. */
inline fun <reified T : Enum<T>> Table.pgEnumeration(name: String, postgresTypeName: String): Column<T> =
    customEnumeration(
        name = name,
        sql = postgresTypeName,
        fromDb = { value -> enumValueOf<T>(value.toString()) },
        toDb = { value -> PGEnum(postgresTypeName, value) }
    )
