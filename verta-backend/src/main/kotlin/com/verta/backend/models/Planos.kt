package com.verta.backend.models

import org.jetbrains.exposed.sql.Table

/**
 * Espelha public.planos — catalogo fixo dos planos que a Verta oferece (Basico/Plus).
 * Os periodos de contratacao (6 meses/1 ano/3 anos) nao mudam os limites do plano,
 * so a data de vencimento da assinatura da empresa (ver [Empresas.periodoPlano]).
 */
object Planos : Table("planos") {
    val id = integer("id").autoIncrement()
    val nome = varchar("nome", 30).uniqueIndex()
    val maxUsuarios = integer("max_usuarios")
    val maxTemplates = integer("max_templates")
    // Se os 5 templates base entregues nesse plano vem com validacao juridica da nossa equipe.
    val validacaoJuridicaBase = bool("validacao_juridica_base").default(false)
    // Quantos templates por mes a equipe juridica se compromete a validar (so Plus, por ora).
    val quotaGeracaoJuridicaMensal = integer("quota_geracao_juridica_mensal").nullable()

    override val primaryKey = PrimaryKey(id)
}

/** Valores aceitos em Empresas.periodoPlano - duracao da assinatura contratada. */
enum class PeriodoPlano(val meses: Long) {
    SEIS_MESES(6),
    UM_ANO(12),
    TRES_ANOS(36);

    companion object {
        fun fromInput(valor: String): PeriodoPlano =
            entries.find { it.name.equals(valor, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Periodo invalido: '$valor' (valores aceitos: ${entries.joinToString { it.name }})"
                )
    }
}
