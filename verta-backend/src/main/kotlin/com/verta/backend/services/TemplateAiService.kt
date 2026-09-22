package com.verta.backend.services

import com.verta.backend.config.AiConfig
import com.verta.backend.dto.TemplateGeracaoIaResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/** Erro esperado na geracao por IA (config ausente, entrada invalida, falha no provedor). */
class TemplateAiException(message: String) : RuntimeException(message)

@Serializable
private data class GroqMensagem(val role: String, val content: String)

@Serializable
private data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMensagem>,
    val temperature: Double = 0.4
)

@Serializable
private data class GroqMensagemResposta(val content: String)

@Serializable
private data class GroqEscolha(val message: GroqMensagemResposta)

@Serializable
private data class GroqChatResponse(val choices: List<GroqEscolha> = emptyList())

/**
 * Gera conteudo de templates de contrato chamando a API de chat completions da Groq
 * (formato compativel com OpenAI). O texto gerado usa a mesma marcacao simples aceita
 * pelo editor de templates do frontend: "**negrito**", "*italico*", "__sublinhado__",
 * linhas "- item", "# titulo" e "[centro]"/"[direita]"/"[justificado]" no inicio da
 * linha para alinhamento, alem de "{{variavel}}" para os campos que mudam por contrato.
 */
class TemplateAiService(private val config: AiConfig) {
    private val logger = LoggerFactory.getLogger(TemplateAiService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    suspend fun gerar(descricao: String): TemplateGeracaoIaResponseDto {
        if (config.groqApiKey.isBlank()) {
            throw TemplateAiException(
                "Geracao por IA nao configurada: defina a variavel de ambiente GROQ_API_KEY no backend."
            )
        }
        if (descricao.isBlank()) {
            throw TemplateAiException("Descreva o tipo de contrato que deseja gerar.")
        }

        val resposta = try {
            client.post(config.groqApiUrl) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${config.groqApiKey}")
                setBody(
                    GroqChatRequest(
                        model = config.groqModel,
                        messages = listOf(
                            GroqMensagem("system", PROMPT_SISTEMA),
                            GroqMensagem("user", descricao.trim())
                        )
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Falha ao chamar a API da Groq", e)
            throw TemplateAiException("Nao foi possivel conectar ao servico de IA. Tente novamente.")
        }

        if (!resposta.status.isSuccess()) {
            val corpo = runCatching { resposta.bodyAsText() }.getOrNull()
            logger.error("Groq retornou erro ${resposta.status}: $corpo")
            throw TemplateAiException("O servico de IA retornou um erro (${resposta.status.value}).")
        }

        val texto = runCatching { resposta.body<GroqChatResponse>() }
            .getOrNull()
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw TemplateAiException("O servico de IA nao retornou conteudo.")

        return montarResultado(texto)
    }

    /** Extrai um nome de template da primeira linha ("# Titulo") e devolve o resto como conteudo. */
    private fun montarResultado(textoGerado: String): TemplateGeracaoIaResponseDto {
        val linhas = textoGerado.lines()
        val primeiraLinha = linhas.firstOrNull { it.isNotBlank() }?.trim() ?: "Novo template"

        val match = REGEX_TITULO.find(primeiraLinha)
        val nome = (match?.groupValues?.get(2)?.trim() ?: primeiraLinha).take(150)
        val conteudo = if (match != null) {
            linhas.drop(1).joinToString("\n").trim('\n')
        } else {
            textoGerado
        }

        return TemplateGeracaoIaResponseDto(nome = nome, conteudo = conteudo.ifBlank { textoGerado })
    }

    companion object {
        private val REGEX_TITULO = Regex("^(#\\s*|t[íi]tulo\\s*:\\s*)(.+)$", RegexOption.IGNORE_CASE)

        private val PROMPT_SISTEMA = """
            Voce e um assistente juridico que redige modelos (templates) de contrato em portugues do Brasil.
            Gere um modelo de contrato completo e profissional a partir da descricao fornecida pelo usuario.

            Regras de formatacao (siga exatamente, nao use Markdown nem HTML):
            - Primeira linha: "# " seguido do titulo do contrato (ex: "# Contrato de Prestacao de Servicos").
            - "**texto**" para negrito, "*texto*" para italico, "__texto__" para sublinhado.
            - Linhas de lista comecam com "- ".
            - Para alinhar um paragrafo, comece a linha com "[centro] ", "[direita] " ou "[justificado] " (sem isso, o padrao e alinhado a esquerda).
            - Use {{nome_da_variavel}} (sem espacos, snake_case, em portugues) para todo dado que varia de contrato para contrato, como {{nome_contratante}}, {{cpf_contratante}}, {{nome_contratada}}, {{cnpj_contratada}}, {{valor_total}}, {{data_assinatura}}, {{prazo_vigencia}}, {{cidade}}, etc. Nunca invente dados reais.
            - Estruture o contrato em clausulas numeradas (ex: "CLAUSULA PRIMEIRA - DO OBJETO"), com paragrafos objetivos.
            - Nao inclua comentarios, explicacoes ou qualquer texto fora do proprio modelo de contrato.
        """.trimIndent()
    }
}
