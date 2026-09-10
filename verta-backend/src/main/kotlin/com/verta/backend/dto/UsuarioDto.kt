package com.verta.backend.dto

import kotlinx.serialization.Serializable

// DTO de resposta - nunca inclui a senha/hash.
@Serializable
data class UsuarioDto(
    val id: Int? = null,
    val empresaId: Int,
    val nome: String,
    val email: String,
    val perfil: String,
    val ativo: Boolean = true,
    val dataCadastro: String? = null
)

@Serializable
data class UsuarioCreateDto(
    val empresaId: Int,
    val nome: String,
    val email: String,
    val senha: String, // texto puro recebido no cadastro, sera hasheado com bcrypt
    val perfil: String,
    val ativo: Boolean = true
)

@Serializable
data class UsuarioUpdateDto(
    val nome: String,
    val email: String,
    val perfil: String,
    val ativo: Boolean,
    val senha: String? = null // se enviado, a senha e atualizada
)
