package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArquivoDto(
    val id: Int? = null,
    val contratoId: Int,
    val versaoId: Int,
    val nomeArquivo: String,
    val caminhoArquivo: String,
    val tipoArquivo: String? = null,
    val tamanho: Long? = null,
    val dataArmazenamento: String? = null
)

@Serializable
data class ArquivoCreateDto(
    val contratoId: Int,
    val versaoId: Int,
    val nomeArquivo: String,
    val caminhoArquivo: String,
    val tipoArquivo: String? = null,
    val tamanho: Long? = null
)
