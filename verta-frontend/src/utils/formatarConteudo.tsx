import type { CSSProperties, ReactNode } from 'react'

// Marcacoes de texto simples suportadas no conteudo de templates/contratos:
// **negrito**, *italico*, __sublinhado__, "- item" (lista), "# titulo" (titulo centralizado)
// e "[centro]"/"[direita]"/"[justificado]" no inicio da linha (alinhamento do paragrafo).
const TOKEN_REGEX = /(\{\{\s*[\w.]+\s*\}\}|\*\*.+?\*\*|__.+?__|\*.+?\*)/g

export type Alinhamento = 'centro' | 'direita' | 'justificado'

const ALINHAMENTO_REGEX = /^\[(centro|direita|justificado)\]\s?/

const ALINHAMENTO_CSS: Record<Alinhamento, CSSProperties['textAlign']> = {
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
