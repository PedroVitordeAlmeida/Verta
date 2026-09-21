import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { Layout } from './components/Layout'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { GerenciadorArquivos } from './pages/GerenciadorArquivos'
import { ContratoDetalhe } from './pages/ContratoDetalhe'
import { GeracaoContrato } from './pages/GeracaoContrato'
import { AssinaturaDigital } from './pages/AssinaturaDigital'
import { PlanosCobranca } from './pages/PlanosCobranca'
import { Configuracoes } from './pages/Configuracoes'

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/arquivos" element={<GerenciadorArquivos />} />
            <Route path="/contratos/gerar" element={<GeracaoContrato />} />
            <Route path="/contratos/:id" element={<ContratoDetalhe />} />
            <Route path="/assinatura" element={<AssinaturaDigital />} />
            <Route path="/planos" element={<PlanosCobranca />} />
            <Route path="/configuracoes" element={<Configuracoes />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
