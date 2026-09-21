import { apiClient } from './client'
import type { ContratoUsuario } from '../types'

export const contratoUsuariosApi = {
  listarPorContrato: (contratoId: number) =>
    apiClient
      .get<ContratoUsuario[]>('/contrato-usuarios', { params: { contratoId } })
      .then((r) => r.data),
  compartilhar: (dto: Omit<ContratoUsuario, 'id' | 'dataVinculo' | 'empresaId'>) =>
    apiClient.post<ContratoUsuario>('/contrato-usuarios', dto).then((r) => r.data),
  atualizarPermissoes: (
    id: number,
    dto: Pick<ContratoUsuario, 'podeVisualizar' | 'podeEditar' | 'podeAssinar' | 'podeExcluir'>
  ) => apiClient.put(`/contrato-usuarios/${id}/permissoes`, dto).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/contrato-usuarios/${id}`).then((r) => r.data)
}
