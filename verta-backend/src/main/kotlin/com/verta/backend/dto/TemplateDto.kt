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
