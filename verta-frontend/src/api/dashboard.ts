import { apiClient } from './client'
import type { DashboardStats } from '../types'

export const dashboardApi = {
  stats: (empresaId?: number) =>
    apiClient.get<DashboardStats>('/dashboard/stats', { params: { empresaId } }).then((r) => r.data)
}
