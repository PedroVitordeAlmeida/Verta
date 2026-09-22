package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmpresaDto(
    val id: Int? = null,
    val nome: String,
    val cnpj: String? = null,
    val dataCadastro: String? = null,
    val planoId: Int? = null,
    val periodoPlano: String? = null,
    val dataInicioPlano: String? = null,
    val dataFimPlano: String? = null
)

@Serializable
data class EmpresaCreateDto(
    val nome: String,
    val cnpj: String? = null
)
