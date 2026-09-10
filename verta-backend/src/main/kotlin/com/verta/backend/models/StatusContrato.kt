package com.verta.backend.models

/**
 * Valores permitidos para contratos.status, espelhando o CHECK constraint
 * "contratos_status_check" que veio no backup VERTA_backup.sql.
 * Qualquer outro valor enviado pela API e rejeitado antes de chegar no banco.
 */
object StatusContrato {
    const val RASCUNHO = "RASCUNHO"
    const val EM_REVISAO = "EM_REVISAO"
    const val FINALIZADO = "FINALIZADO"
    const val ARQUIVADO = "ARQUIVADO"
    const val CANCELADO = "CANCELADO"

    val VALORES_VALIDOS = setOf(RASCUNHO, EM_REVISAO, FINALIZADO, ARQUIVADO, CANCELADO)

    fun isValido(status: String): Boolean = status in VALORES_VALIDOS
}
