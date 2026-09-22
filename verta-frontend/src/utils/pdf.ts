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

interface LinhaPdf {
  texto: string
  alinhamento?: Alinhamento
}

/** Gera um PDF simples (titulo + conteudo em paragrafos, respeitando o alinhamento) e dispara o download. */
export function baixarContratoPdf(titulo: string, conteudo: string) {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  const margem = 48
  const larguraPagina = doc.internal.pageSize.getWidth()
  const larguraUtil = larguraPagina - margem * 2
  const alturaPagina = doc.internal.pageSize.getHeight()

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(14)
  doc.text(titulo, margem, margem)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)

  const linhas: LinhaPdf[] = []
  for (const paragrafoBruto of conteudo.split('\n')) {
    const { texto: semAlinhamento, alinhamento } = interpretarAlinhamento(paragrafoBruto)
    const paragrafo = textoSemMarcacoes(semAlinhamento)
    if (paragrafo.trim() === '') {
      linhas.push({ texto: '' })
    } else {
      const quebradas = doc.splitTextToSize(paragrafo, larguraUtil) as string[]
      quebradas.forEach((texto) => linhas.push({ texto, alinhamento }))
    }
  }

  let y = margem + 28
  for (const linha of linhas) {
    if (y > alturaPagina - margem) {
      doc.addPage()
      y = margem
    }
    if (linha.texto === '') {
      y += 16
      continue
    }

    if (linha.alinhamento === 'centro') {
      doc.text(linha.texto, larguraPagina / 2, y, { align: 'center' })
    } else if (linha.alinhamento === 'direita') {
      doc.text(linha.texto, larguraPagina - margem, y, { align: 'right' })
    } else if (linha.alinhamento === 'justificado') {
      doc.text(linha.texto, margem, y, { align: 'justify', maxWidth: larguraUtil })
    } else {
      doc.text(linha.texto, margem, y)
    }
    y += 16
  }

  const nomeArquivo = titulo.replace(/[^\w\-]+/g, '_').replace(/_+/g, '_') || 'contrato'
  doc.save(`${nomeArquivo}.pdf`)
}
