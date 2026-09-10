package com.verta.backend.dto

import kotlinx.serialization.Serializable

// Define as permissoes de um usuario sobre um contrato especifico.
@Serializable
data class ContratoUsuarioDto(
    val id: Int? = null,
    val contratoId: Int,
    val usuarioId: Int,
    val podeVisualizar: Boolean = true,
    val podeEditar: Boolean = false,
    val podeAssinar: Boolean = false,
    val podeExcluir: Boolean = false,
    val dataVinculo: String? = null,
    val empresaId: Int? = null
)

@Serializable
data class ContratoUsuarioCreateDto(
    val contratoId: Int,
    val usuarioId: Int,
    val empresaId: Int? = null,
    val podeVisualizar: Boolean = true,
    val podeEditar: Boolean = false,
    val podeAssinar: Boolean = false,
    val podeExcluir: Boolean = false
)

@Serializable
data class ContratoUsuarioPermissoesUpdateDto(
    val podeVisualizar: Boolean,
    val podeEditar: Boolean,
    val podeAssinar: Boolean,
    val podeExcluir: Boolean
)
