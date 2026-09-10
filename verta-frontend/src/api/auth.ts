import { apiClient } from './client'
import type { LoginResponse } from '../types'

export async function login(email: string, senha: string): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/auth/login', { email, senha })
  return data
}
