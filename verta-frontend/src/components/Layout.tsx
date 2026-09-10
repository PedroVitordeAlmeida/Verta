import { Outlet, useLocation, Navigate } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { TopBar } from './TopBar'
import { useAuth } from '../context/AuthContext'

const TITULOS: Record<string, string> = {
  '/': 'Dashboard',
  '/arquivos': 'Gerenciador de Arquivos',
  '/contratos/gerar': 'Geração de Contrato',
  '/assinatura': 'Assinatura Digital',
  '/planos': 'Planos e Cobrança',
  '/configuracoes': 'Configurações'
}

export function Layout() {
  const { usuario, logout } = useAuth()
  const location = useLocation()

  if (!usuario) {
    return <Navigate to="/login" replace />
  }

  const titulo = TITULOS[location.pathname] ?? 'Verta'

  return (
    <div className="app-shell">
      <Sidebar onLogout={logout} nomeUsuario={usuario.nome} />
      <div className="main-area">
        <TopBar titulo={titulo} nomeEmpresa={usuario.nome} perfil={usuario.perfil} />
        <div className="page-content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
