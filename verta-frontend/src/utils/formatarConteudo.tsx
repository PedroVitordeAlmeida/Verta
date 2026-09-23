import type { CSSProperties, ReactNode } from 'react'

// Marcacoes de texto simples suportadas no conteudo de templates/contratos:
// **negrito**, *italico*, __sublinhado__, "- item" (lista), "# titulo" (titulo centralizado)
// e "[centro]"/"[direita]"/"[justificado]" no inicio da linha (alinhamento do paragrafo).
const TOKEN_REGEX = /(\{\{\s*[\w.]+\s*\}\}|\*\*.+?\*\*|__.+?__|\*.+?\*)/g

export type Alinhamento = 'centro' | 'direita' | 'justificado'

const ALINHAMENTO_REGEX = /^\[(centro|direita|justificado)\]\s?/

export const ALINHAMENTO_CSS: Record<Alinhamento, CSSProperties['textAlign']> = {
  centro: 'center',
  direita: 'right',
  justificado: 'justify'
}

/** Adiciona/troca (tag) ou remove (tag=null) a marcacao de alinhamento no inicio de uma linha. */
export function definirAlinhamentoLinha(linha: string, tag: Alinhamento | null): string {
  const semTag = linha.replace(ALINHAMENTO_REGEX, '')
  return tag ? `[${tag}] ${semTag}` : semTag
}

/** Separa a marcacao de alinhamento (se houver) do restante do texto da linha. */
export function interpretarAlinhamento(linha: string): { texto: string; alinhamento?: Alinhamento } {
  const match = linha.match(ALINHAMENTO_REGEX)
  if (!match) return { texto: linha }
  return { texto: linha.slice(match[0].length), alinhamento: match[1] as Alinhamento }
}

function extrairAlinhamento(linha: string): { texto: string; estilo?: CSSProperties } {
  const { texto, alinhamento } = interpretarAlinhamento(linha)
  return alinhamento ? { texto, estilo: { textAlign: ALINHAMENTO_CSS[alinhamento] } } : { texto }
}

function renderizarInline(texto: string, valores?: Record<string, string>): ReactNode[] {
  return texto.split(TOKEN_REGEX).map((parte, i) => {
    if (!parte) return null

    if (/^\{\{/.test(parte)) {
      const nomeVar = parte.replace(/[{}]/g, '').trim()
      if (!valores) return <span key={i}>{parte}</span>
      const valor = valores[nomeVar]
      return valor ? (
        <span key={i} className="placeholder-filled">
          {valor}
        </span>
      ) : (
        <span key={i} className="placeholder-empty">{`{{${nomeVar}}}`}</span>
      )
    }
    if (parte.startsWith('**') && parte.endsWith('**') && parte.length >= 4) {
      return <strong key={i}>{parte.slice(2, -2)}</strong>
    }
    if (parte.startsWith('__') && parte.endsWith('__') && parte.length >= 4) {
      return <u key={i}>{parte.slice(2, -2)}</u>
    }
    if (parte.startsWith('*') && parte.endsWith('*') && parte.length >= 2) {
      return <em key={i}>{parte.slice(1, -1)}</em>
    }
    return <span key={i}>{parte}</span>
  })
}

/** Renderiza o conteudo de um template/contrato respeitando as marcacoes de formatacao e (opcionalmente) preenchendo {{variaveis}}. */
export function renderizarConteudoFormatado(conteudo: string, valores?: Record<string, string>): ReactNode[] {
  const blocos: ReactNode[] = []
  let listaAtual: { texto: string; estilo?: CSSProperties }[] = []

  function fecharLista(key: string) {
    if (listaAtual.length === 0) return
    blocos.push(
      <ul key={key}>
        {listaAtual.map((item, i) => (
          <li key={i} style={item.estilo}>
            {renderizarInline(item.texto, valores)}
          </li>
        ))}
      </ul>
    )
    listaAtual = []
  }

  conteudo.split('\n').forEach((linhaOriginal, i) => {
    const { texto: linha, estilo } = extrairAlinhamento(linhaOriginal)

    if (linha.startsWith('- ')) {
      listaAtual.push({ texto: linha.slice(2), estilo })
      return
    }
    fecharLista(`lista-${i}`)

    if (linha.startsWith('# ')) {
      blocos.push(
        <h1 key={i} style={estilo}>
          {renderizarInline(linha.slice(2), valores)}
        </h1>
      )
    } else {
      blocos.push(
        <p key={i} style={estilo}>
          {renderizarInline(linha, valores)}
        </p>
      )
    }
  })
  fecharLista('lista-final')

  return blocos
}

// ---------------------------------------------------------------------------
// Conteudo em HTML (editor Tiptap) - novo formato usado por templates criados/
// editados a partir de agora. O formato antigo acima (**negrito**, "# titulo"...)
// continua sendo lido normalmente (templates/contratos ja existentes), so nao e
// mais o formato gravado por um template novo.
// ---------------------------------------------------------------------------

/** Heuristica simples: conteudo em HTML sempre começa com uma tag de bloco (o Tiptap sempre envolve tudo em ao menos um `<p>`). */
export function pareceHtml(conteudo: string): boolean {
  return /^\s*<[a-z][\s\S]*>/i.test(conteudo)
}

function escapeHtml(texto: string): string {
  return texto
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

function inlineParaHtml(texto: string): string {
  return escapeHtml(texto)
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/__(.+?)__/g, '<u>$1</u>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
}

/** Converte um template/contrato no formato antigo (marcacoes de texto) para HTML, pra poder ser aberto no editor Tiptap. */
export function converterMarkupAntigoParaHtml(conteudo: string): string {
  const partesHtml: string[] = []
  let listaAtual: string[] = []

  function fecharLista() {
    if (listaAtual.length === 0) return
    partesHtml.push(`<ul>${listaAtual.map((item) => `<li>${item}</li>`).join('')}</ul>`)
    listaAtual = []
  }

  conteudo.split('\n').forEach((linhaOriginal) => {
    const { texto: linha, alinhamento } = interpretarAlinhamento(linhaOriginal)
    const estilo = alinhamento ? ` style="text-align: ${ALINHAMENTO_CSS[alinhamento]}"` : ''

    if (linha.startsWith('- ')) {
      listaAtual.push(inlineParaHtml(linha.slice(2)))
      return
    }
    fecharLista()

    if (linha.startsWith('# ')) {
      partesHtml.push(`<h1${estilo}>${inlineParaHtml(linha.slice(2))}</h1>`)
    } else {
      partesHtml.push(`<p${estilo}>${inlineParaHtml(linha)}</p>`)
    }
  })
  fecharLista()

  return partesHtml.join('') || '<p></p>'
}

/** Substitui {{variavel}} por span preenchido/vazio direto na string HTML (os tokens sempre ficam em um unico no de texto). */
function aplicarValoresNoHtml(html: string, valores?: Record<string, string>): string {
  if (!valores) return html
  return html.replace(/\{\{\s*([\w.]+)\s*\}\}/g, (match, nomeVar: string) => {
    const valor = valores[nomeVar]
    return valor
      ? `<span class="placeholder-filled">${escapeHtml(valor)}</span>`
      : `<span class="placeholder-empty">${match}</span>`
  })
}

/**
 * Renderiza o conteudo de um template/contrato, aceitando tanto HTML (formato novo, vindo
 * do editor Tiptap) quanto o formato antigo de marcacoes de texto - e o unico ponto que as
 * telas de contrato/geracao devem usar pra exibir `conteudo`/`versoes_contrato.conteudo`.
 */
export function ConteudoRenderizado({
  conteudo,
  valores
}: {
  conteudo: string
  valores?: Record<string, string>
}) {
  if (pareceHtml(conteudo)) {
    return <div dangerouslySetInnerHTML={{ __html: aplicarValoresNoHtml(conteudo, valores) }} />
  }
  return <>{renderizarConteudoFormatado(conteudo, valores)}</>
}
