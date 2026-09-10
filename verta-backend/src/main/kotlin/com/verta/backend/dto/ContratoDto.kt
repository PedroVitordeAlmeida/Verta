package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContratoDto(
    val id: Int? = null,
    val empresaId: Int,
    val templateId: Int? = null,
    val titulo: String,
    val tipo: String? = null,
    val status: String = "RASCUNHO",
    val criadoPor: Int,
    val dataCriacao: String? = null,
    val dataAtualizacao: String? = null
)

@Serializable
data class ContratoCreateDto(
    val empresaId: Int,
    val templateId: Int? = null,
    val titulo: String,
    val tipo: String? = null,
    val criadoPor: Int
)

@Serializable
data class ContratoStatusUpdateDto(
    val status: String
)

// Usado pela tela "Geracao de Contrato": recebe o template + os valores
// digitados nos campos {{variavel}} e gera o contrato com o conteudo final.
@Serializable
data class GerarContratoDto(
    val empresaId: Int,
    val templateId: Int,
    val titulo: String,
    val tipo: String? = null,
    val criadoPor: Int,
    val valores: Map<String, String> // ex: {"contratante_nome": "Empresa Ltda."}
)
