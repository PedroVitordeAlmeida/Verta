package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class TemplateDto(
    val id: Int? = null,
    val empresaId: Int,
    val nome: String,
    val descricao: String? = null,
    val conteudo: String,
    val ativo: Boolean = true,
    val dataCriacao: String? = null
)

@Serializable
data class TemplateCreateDto(
    val empresaId: Int,
    val nome: String,
    val descricao: String? = null,
    val conteudo: String,
    val ativo: Boolean = true
)

/** Pedido de geracao de conteudo de template via IA: uma descricao em linguagem natural do contrato desejado. */
@Serializable
data class TemplateGeracaoIaRequestDto(
    val descricao: String
)

/** Resultado da geracao: nome sugerido e conteudo pronto para revisao/edicao antes de salvar. */
@Serializable
data class TemplateGeracaoIaResponseDto(
    val nome: String,
    val conteudo: String
)
