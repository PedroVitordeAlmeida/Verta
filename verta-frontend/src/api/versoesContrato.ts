import { apiClient } from './client'
import type { VersaoContrato } from '../types'

export const versoesContratoApi = {
  listarPorContrato: (contratoId: number) =>
    apiClient.get<VersaoContrato[]>('/versoes-contrato', { params: { contratoId } }).then((r) => r.data),
  criar: (dto: { contratoId: number; conteudo: string; criadoPor: number }) =>
    apiClient.post<VersaoContrato>('/versoes-contrato', dto).then((r) => r.data)
}
