import { apiClient } from './client'
import type { Plano } from '../types'

export const planosApi = {
  listar: () => apiClient.get<Plano[]>('/planos').then((r) => r.data)
}
