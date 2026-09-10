package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class VersaoContratoDto(
    val id: Int? = null,
    val contratoId: Int,
    val numeroVersao: Int,
    val conteudo: String,
    val criadoPor: Int,
    val dataCriacao: String? = null
)

@Serializable
data class VersaoContratoCreateDto(
    val contratoId: Int,
    val conteudo: String,
    val criadoPor: Int
)
