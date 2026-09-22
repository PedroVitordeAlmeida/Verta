import { apiClient } from './client'
import type { Template } from '../types'

export interface TemplateGeradoPorIa {
  nome: string
  conteudo: string
}

export const templatesApi = {
  listar: (empresaId?: number) =>
    apiClient.get<Template[]>('/templates', { params: { empresaId } }).then((r) => r.data),
  buscar: (id: number) => apiClient.get<Template>(`/templates/${id}`).then((r) => r.data),
  criar: (dto: Omit<Template, 'id' | 'dataCriacao'>) =>
    apiClient.post<Template>('/templates', dto).then((r) => r.data),
  atualizar: (id: number, dto: Omit<Template, 'id' | 'dataCriacao'>) =>
    apiClient.put(`/templates/${id}`, dto).then((r) => r.data),
  remover: (id: number) => apiClient.delete(`/templates/${id}`).then((r) => r.data),
  gerarComIa: (descricao: string) =>
    apiClient.post<TemplateGeradoPorIa>('/templates/gerar-ia', { descricao }).then((r) => r.data)
}

/** Extrai os nomes das variaveis {{assim}} usadas no conteudo de um template. */
export function extrairVariaveis(conteudo: string): string[] {
  const encontradas = conteudo.match(/{{\s*[\w.]+\s*}}/g) || []
  const nomes = encontradas.map((v) => v.replace(/[{}]/g, '').trim())
  return Array.from(new Set(nomes))
}
