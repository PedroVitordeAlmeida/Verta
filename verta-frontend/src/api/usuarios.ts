import { apiClient } from './client'
import type { Usuario } from '../types'

export const usuariosApi = {
  listar: (empresaId?: number) =>
    apiClient.get<Usuario[]>('/usuarios', { params: { empresaId } }).then((r) => r.data),
  buscar: (id: number) => apiClient.get<Usuario>(`/usuarios/${id}`).then((r) => r.data),
  criar: (dto: Usuario & { senha: string }) => apiClient.post<Usuario>('/usuarios', dto).then((r) => r.data),
  atualizar: (id: number, dto: Partial<Usuario> & { senha?: string }) =>
    apiClient.put(`/usuarios/${id}`, dto).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/usuarios/${id}`).then((r) => r.data)
}
