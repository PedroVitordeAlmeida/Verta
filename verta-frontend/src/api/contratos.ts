import { apiClient } from './client'
import type { Contrato, StatusContrato } from '../types'

export interface GerarContratoPayload {
  empresaId: number
  templateId: number
  titulo: string
  tipo?: string
  criadoPor: number
  valores: Record<string, string>
}

export const contratosApi = {
  listar: (empresaId?: number, status?: StatusContrato) =>
    apiClient.get<Contrato[]>('/contratos', { params: { empresaId, status } }).then((r) => r.data),
  buscar: (id: number) => apiClient.get<Contrato>(`/contratos/${id}`).then((r) => r.data),
  criar: (dto: Omit<Contrato, 'id' | 'status' | 'dataCriacao' | 'dataAtualizacao'>) =>
    apiClient.post<Contrato>('/contratos', dto).then((r) => r.data),
  gerarDeTemplate: (dto: GerarContratoPayload) =>
    apiClient.post<Contrato>('/contratos/gerar', dto).then((r) => r.data),
  atualizarStatus: (id: number, status: StatusContrato) =>
    apiClient.patch(`/contratos/${id}/status`, { status }).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/contratos/${id}`).then((r) => r.data)
}
