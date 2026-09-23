import jsPDF from 'jspdf'
import { interpretarAlinhamento, pareceHtml, type Alinhamento } from './formatarConteudo'

/** Remove as marcacoes de formatacao (**negrito**, *italico*, __sublinhado__, # titulo, - item) antes de exportar. */
function textoSemMarcacoes(linha: string): string {
  return linha
    .replace(/^#\s+/, '')
    .replace(/^- /, '• ')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/__(.+?)__/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
}

interface ParagrafoPdf {
  texto: string
  alinhamento?: Alinhamento
}

/** Formato antigo (marcacoes de texto): um paragrafo por linha. */
function extrairParagrafosDeMarkup(conteudo: string): ParagrafoPdf[] {
  return conteudo.split('\n').map((linhaBruta) => {
    const { texto: semAlinhamento, alinhamento } = interpretarAlinhamento(linhaBruta)
    return { texto: textoSemMarcacoes(semAlinhamento), alinhamento }
  })
}

const ALINHAMENTO_CSS_INVERSO: Record<string, Alinhamento> = {
  center: 'centro',
  right: 'direita',
  justify: 'justificado'
}

function alinhamentoDoElemento(el: Element): Alinhamento | undefined {
  const valor = (el as HTMLElement).style?.textAlign
  return valor ? ALINHAMENTO_CSS_INVERSO[valor] : undefined
}

/** Formato novo (HTML do editor Tiptap): um paragrafo por bloco (p/h1/li), sem renderizar negrito/italico (mesma fidelidade do formato antigo, que tambem so imprime texto puro). */
function extrairParagrafosDeHtml(html: string): ParagrafoPdf[] {
  const documento = new DOMParser().parseFromString(html, 'text/html')
  const paragrafos: ParagrafoPdf[] = []

  documento.body.childNodes.forEach((no) => {
    if (!(no instanceof HTMLElement)) return
    const tag = no.tagName.toLowerCase()

    if (tag === 'ul' || tag === 'ol') {
      no.querySelectorAll(':scope > li').forEach((li) => {
        paragrafos.push({
          texto: `• ${(li.textContent ?? '').trim()}`,
          alinhamento: alinhamentoDoElemento(li) ?? alinhamentoDoElemento(no)
        })
      })
      return
    }

    paragrafos.push({ texto: (no.textContent ?? '').trim(), alinhamento: alinhamentoDoElemento(no) })
  })

  return paragrafos
}

const ALTURA_LINHA = 16

/** Gera um PDF com apenas o conteudo do contrato (sem o titulo/nome), respeitando o alinhamento de cada paragrafo. */
export function baixarContratoPdf(titulo: string, conteudo: string) {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  const margem = 48
  const larguraPagina = doc.internal.pageSize.getWidth()
  const larguraUtil = larguraPagina - margem * 2
  const alturaPagina = doc.internal.pageSize.getHeight()

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)

  let y = margem

  function garantirEspaco(alturaNecessaria: number) {
    if (y + alturaNecessaria > alturaPagina - margem) {
      doc.addPage()
      y = margem
    }
  }

  const paragrafos = pareceHtml(conteudo) ? extrairParagrafosDeHtml(conteudo) : extrairParagrafosDeMarkup(conteudo)

  for (const { texto: paragrafo, alinhamento } of paragrafos) {
    if (paragrafo.trim() === '') {
      y += ALTURA_LINHA
      continue
    }

    const quebradas = doc.splitTextToSize(paragrafo, larguraUtil) as string[]

    if (alinhamento === 'justificado' && quebradas.length > 1) {
      // O modo "justify" do jsPDF so estica o espacamento entre palavras quando
      // recebe o paragrafo inteiro (array de linhas) de uma vez, com maxWidth.
      garantirEspaco(quebradas.length * ALTURA_LINHA)
      doc.text(quebradas, margem, y, { align: 'justify', maxWidth: larguraUtil })
      y += quebradas.length * ALTURA_LINHA
      continue
    }

    for (const linha of quebradas) {
      garantirEspaco(ALTURA_LINHA)
      const alinhamentoEfetivo: Alinhamento | undefined =
        alinhamento === 'justificado' ? undefined : alinhamento
      if (alinhamentoEfetivo === 'centro') {
        doc.text(linha, larguraPagina / 2, y, { align: 'center' })
      } else if (alinhamentoEfetivo === 'direita') {
        doc.text(linha, larguraPagina - margem, y, { align: 'right' })
      } else {
        doc.text(linha, margem, y)
      }
      y += ALTURA_LINHA
    }
  }

  const nomeArquivo = titulo.replace(/[^\w\-]+/g, '_').replace(/_+/g, '_') || 'contrato'
  doc.save(`${nomeArquivo}.pdf`)
}
