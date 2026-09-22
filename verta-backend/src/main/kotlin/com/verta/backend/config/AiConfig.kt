package com.verta.backend.config

import io.ktor.server.config.*

/**
 * Configuracao do provedor de IA usado para gerar conteudo de templates (Groq,
 * API compativel com o formato de chat completions da OpenAI).
 *
 * A chave real fica so em variavel de ambiente (GROQ_API_KEY), nunca em arquivo
 * versionado. Sem ela, [groqApiKey] fica vazio e a geracao por IA e desabilitada.
 */
class AiConfig(config: ApplicationConfig) {
    val groqApiKey: String = config.propertyOrNull("ai.groqApiKey")?.getString() ?: ""
    val groqApiUrl: String = config.propertyOrNull("ai.groqApiUrl")?.getString()
        ?: "https://api.groq.com/openai/v1/chat/completions"
    val groqModel: String = config.propertyOrNull("ai.groqModel")?.getString() ?: "llama-3.3-70b-versatile"
}
