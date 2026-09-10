import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { login as loginRequest } from '../api/auth'

interface UsuarioLogado {
  usuarioId: number
  empresaId: number
  nome: string
  perfil: string
}

interface AuthContextValue {
  usuario: UsuarioLogado | null
  carregando: boolean
  login: (email: string, senha: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function lerUsuarioSalvo(): UsuarioLogado | null {
  const bruto = localStorage.getItem('verta_usuario')
  if (!bruto) return null
  try {
    return JSON.parse(bruto) as UsuarioLogado
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioLogado | null>(lerUsuarioSalvo)
  const [carregando, setCarregando] = useState(false)

  const login = useCallback(async (email: string, senha: string) => {
    setCarregando(true)
    try {
      const resposta = await loginRequest(email, senha)
      const usuarioLogado: UsuarioLogado = {
        usuarioId: resposta.usuarioId,
        empresaId: resposta.empresaId,
        nome: resposta.nome,
        perfil: resposta.perfil
      }
      localStorage.setItem('verta_token', resposta.token)
      localStorage.setItem('verta_usuario', JSON.stringify(usuarioLogado))
      setUsuario(usuarioLogado)
    } finally {
      setCarregando(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('verta_token')
    localStorage.removeItem('verta_usuario')
    setUsuario(null)
  }, [])

  return (
    <AuthContext.Provider value={{ usuario, carregando, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth precisa ser usado dentro de um AuthProvider')
  return ctx
}
