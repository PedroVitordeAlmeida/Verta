import type { StatusContrato } from '../types'

const LABELS: Record<StatusContrato, string> = {
  RASCUNHO: 'Rascunho',
  EM_REVISAO: 'Em revisão',
  FINALIZADO: 'Finalizado',
  ARQUIVADO: 'Arquivado',
  CANCELADO: 'Cancelado'
}

export function StatusBadge({ status }: { status: StatusContrato }) {
  const classe = `badge badge-${status.toLowerCase()}`
  return <span className={classe}>{LABELS[status] ?? status}</span>
}
