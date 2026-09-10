import { apiClient } from './client'
import type { Arquivo } from '../types'

export const arquivosApi = {
  listarPorContrato: (contratoId: number) =>
    apiClient.get<Arquivo[]>('/arquivos', { params: { contratoId } }).then((r) => r.data),
  listarPorVersao: (versaoId: number) =>
    apiClient.get<Arquivo[]>('/arquivos', { params: { versaoId } }).then((r) => r.data),
  listarTodos: () => apiClient.get<Arquivo[]>('/arquivos').then((r) => r.data),
  criar: (dto: Omit<Arquivo, 'id' | 'dataArmazenamento'>) =>
    apiClient.post<Arquivo>('/arquivos', dto).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/arquivos/${id}`).then((r) => r.data)
}
