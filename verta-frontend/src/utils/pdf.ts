import jsPDF from 'jspdf'

/** Remove as marcacoes de formatacao (**negrito**, *italico*, __sublinhado__, # titulo, - item) antes de exportar. */
function textoSemMarcacoes(linha: string): string {
  return linha
    .replace(/^#\s+/, '')
    .replace(/^- /, '• ')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/__(.+?)__/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
}

/** Gera um PDF simples (titulo + conteudo em paragrafos) e dispara o download no navegador. */
export function baixarContratoPdf(titulo: string, conteudo: string) {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  const margem = 48
  const larguraUtil = doc.internal.pageSize.getWidth() - margem * 2
  const alturaPagina = doc.internal.pageSize.getHeight()

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(14)
  doc.text(titulo, margem, margem)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)

  const linhas: string[] = []
  for (const paragrafoBruto of conteudo.split('\n')) {
    const paragrafo = textoSemMarcacoes(paragrafoBruto)
    if (paragrafo.trim() === '') {
      linhas.push('')
    } else {
      linhas.push(...(doc.splitTextToSize(paragrafo, larguraUtil) as string[]))
    }
  }

  let y = margem + 28
  for (const linha of linhas) {
    if (y > alturaPagina - margem) {
      doc.addPage()
      y = margem
    }
    doc.text(linha, margem, y)
    y += 16
  }

  const nomeArquivo = titulo.replace(/[^\w\-]+/g, '_').replace(/_+/g, '_') || 'contrato'
  doc.save(`${nomeArquivo}.pdf`)
}
