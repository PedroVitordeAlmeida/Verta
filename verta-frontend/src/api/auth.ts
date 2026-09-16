import { apiClient } from './client'
import type { LoginResponse } from '../types'

export async function login(email: string, senha: string): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/auth/login', { email, senha })
  return data
}

// Avisa o backend para liberar a sessao (token_sessao) deste usuario.
export async function logout(): Promise<void> {
  await apiClient.post('/auth/logout')
}
