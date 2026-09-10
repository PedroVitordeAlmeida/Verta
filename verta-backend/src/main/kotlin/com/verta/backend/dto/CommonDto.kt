package com.verta.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val message: String
)

@Serializable
data class MessageResponseDto(
    val message: String
)
