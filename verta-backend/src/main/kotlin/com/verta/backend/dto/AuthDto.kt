package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val senha: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val usuarioId: Int,
    val empresaId: Int,
    val nome: String,
    val perfil: String
)
