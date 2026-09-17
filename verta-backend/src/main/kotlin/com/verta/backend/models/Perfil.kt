package com.verta.backend.models

/** Espelha o ENUM nativo public.tipo_perfil. */
enum class Perfil {
    ADMIN,
    COMUM;

    companion object {
        /** Converte texto vindo de fora (JSON) para o enum, com mensagem clara se invalido. */
        fun fromInput(valor: String): Perfil =
            entries.find { it.name.equals(valor, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Perfil invalido: '$valor' (valores aceitos: ${entries.joinToString { it.name }})"
                )
    }
}
