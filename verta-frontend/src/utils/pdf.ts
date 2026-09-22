import jsPDF from 'jspdf'
import { interpretarAlinhamento, type Alinhamento } from './formatarConteudo'

/** Remove as marcacoes de formatacao (**negrito**, *italico*, __sublinhado__, # titulo, - item) antes de exportar. */
function textoSemMarcacoes(linha: string): string {
  return linha
    .replace(/^#\s+/, '')
    .replace(/^- /, '• ')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/__(.+?)__/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
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

  for (const paragrafoBruto of conteudo.split('\n')) {
    const { texto: semAlinhamento, alinhamento } = interpretarAlinhamento(paragrafoBruto)
    const paragrafo = textoSemMarcacoes(semAlinhamento)

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
