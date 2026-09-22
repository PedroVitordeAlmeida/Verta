import { apiClient } from './client'
import type { Empresa, PeriodoPlano } from '../types'

export const empresasApi = {
  listar: () => apiClient.get<Empresa[]>('/empresas').then((r) => r.data),
  buscar: (id: number) => apiClient.get<Empresa>(`/empresas/${id}`).then((r) => r.data),
  criar: (dto: Empresa) => apiClient.post<Empresa>('/empresas', dto).then((r) => r.data),
  atualizar: (id: number, dto: Empresa) => apiClient.put(`/empresas/${id}`, dto).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/empresas/${id}`).then((r) => r.data),
  atribuirPlano: (id: number, planoId: number, periodo: PeriodoPlano) =>
    apiClient.put<Empresa>(`/empresas/${id}/plano`, { planoId, periodo }).then((r) => r.data)
}
