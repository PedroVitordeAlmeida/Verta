import type { ReactNode } from 'react'

// Marcacoes de texto simples suportadas no conteudo de templates/contratos:
// **negrito**, *italico*, __sublinhado__, "- item" (lista) e "# titulo" (titulo centralizado).
const TOKEN_REGEX = /(\{\{\s*[\w.]+\s*\}\}|\*\*.+?\*\*|__.+?__|\*.+?\*)/g

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
  let listaAtual: string[] = []

  function fecharLista(key: string) {
    if (listaAtual.length === 0) return
    blocos.push(
      <ul key={key}>
        {listaAtual.map((item, i) => (
          <li key={i}>{renderizarInline(item, valores)}</li>
        ))}
      </ul>
    )
    listaAtual = []
  }

  conteudo.split('\n').forEach((linha, i) => {
    if (linha.startsWith('- ')) {
      listaAtual.push(linha.slice(2))
      return
    }
    fecharLista(`lista-${i}`)

    if (linha.startsWith('# ')) {
      blocos.push(<h1 key={i}>{renderizarInline(linha.slice(2), valores)}</h1>)
    } else {
      blocos.push(<p key={i}>{renderizarInline(linha, valores)}</p>)
    }
  })
  fecharLista('lista-final')

  return blocos
}
