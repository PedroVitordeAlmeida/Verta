package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlanoDto(
    val id: Int,
    val nome: String,
    val maxUsuarios: Int,
    val maxTemplates: Int,
    val validacaoJuridicaBase: Boolean,
    val quotaGeracaoJuridicaMensal: Int? = null
)

/** Atribuicao/troca de plano de uma empresa - restrita a usuarios SUPERADMIN. */
@Serializable
data class EmpresaAtribuirPlanoDto(
    val planoId: Int,
    val periodo: String
)
