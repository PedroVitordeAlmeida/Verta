// Espelham os DTOs do backend (Kotlin/Ktor) - ver verta-backend/src/main/kotlin/com/verta/backend/dto

export interface Empresa {
  id?: number
  nome: string
  cnpj?: string | null
  dataCadastro?: string | null
  planoId?: number | null
  periodoPlano?: string | null
  dataInicioPlano?: string | null
  dataFimPlano?: string | null
}

// Valores aceitos em Empresa.periodoPlano
export type PeriodoPlano = 'SEIS_MESES' | 'UM_ANO' | 'TRES_ANOS'

export interface Plano {
  id: number
  nome: string
  maxUsuarios: number
  maxTemplates: number
  validacaoJuridicaBase: boolean
  quotaGeracaoJuridicaMensal?: number | null
}

export interface Usuario {
  id?: number
  empresaId: number
  nome: string
  email: string
  perfil: string
  ativo: boolean
  dataCadastro?: string | null
}

export interface Template {
  id?: number
  empresaId: number
  nome: string
  descricao?: string | null
  conteudo: string
  ativo: boolean
  dataCriacao?: string | null
  validadoJuridicamente?: boolean
  origemSistema?: boolean
}

// Valores fixos aceitos pelo banco (CHECK constraint em contratos.status)
export type StatusContrato = 'RASCUNHO' | 'EM_REVISAO' | 'FINALIZADO' | 'ARQUIVADO' | 'CANCELADO'

export interface Contrato {
  id?: number
  empresaId: number
  templateId?: number | null
  titulo: string
  tipo?: string | null
  status: StatusContrato
  criadoPor: number
  dataCriacao?: string | null
  dataAtualizacao?: string | null
}

export interface VersaoContrato {
  id?: number
  contratoId: number
  numeroVersao: number
  conteudo: string
  criadoPor: number
  dataCriacao?: string | null
}

export interface Arquivo {
  id?: number
  contratoId: number
  versaoId: number
  nomeArquivo: string
  caminhoArquivo: string
  tipoArquivo?: string | null
  tamanho?: number | null
  dataArmazenamento?: string | null
}

export interface ContratoUsuario {
  id?: number
  contratoId: number
  usuarioId: number
  podeVisualizar: boolean
  podeEditar: boolean
  podeAssinar: boolean
  podeExcluir: boolean
  dataVinculo?: string | null
  empresaId?: number | null
}

export interface DashboardStats {
  contratosAtivos: number
  aguardandoAssinatura: number
  assinadosEsteMes: number
  templatesCadastrados: number
}

export interface LoginResponse {
  token: string
  usuarioId: number
  empresaId: number
  nome: string
  perfil: string
}
