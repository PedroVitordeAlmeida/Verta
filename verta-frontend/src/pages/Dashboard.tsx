import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { dashboardApi } from '../api/dashboard'
import { contratosApi } from '../api/contratos'
import { StatCard } from '../components/StatCard'
import { StatusBadge } from '../components/StatusBadge'
import { useAuth } from '../context/AuthContext'
import type { Contrato, DashboardStats } from '../types'

export function Dashboard() {
  const { usuario } = useAuth()
  const navigate = useNavigate()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [contratosRecentes, setContratosRecentes] = useState<Contrato[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (!usuario) return
    let ativo = true

    async function carregar() {
      try {
        const [statsResp, contratosResp] = await Promise.all([
          dashboardApi.stats(usuario!.empresaId),
          contratosApi.listar(usuario!.empresaId)
        ])
        if (!ativo) return
        setStats(statsResp)
        setContratosRecentes(contratosResp.slice(0, 5))
      } catch {
        if (ativo) setErro('Não foi possível carregar os dados do dashboard.')
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregar()
    return () => {
      ativo = false
    }
  }, [usuario])

  async function irParaNovoContrato() {
    navigate('/contratos/gerar')
  }

  async function irParaUploadTemplate() {
    navigate('/arquivos')
  }

  async function irParaAguardandoAssinatura() {
    navigate('/assinatura')
  }

  if (carregando) {
    return <div className="loading-text">Carregando dashboard...</div>
  }

  return (
    <div>
      {erro && <div className="status-message error">{erro}</div>}

      <div className="stats-grid">
        <StatCard value={stats?.contratosAtivos ?? 0} label="Contratos ativos" color="var(--verta-blue)" />
        <StatCard
          value={stats?.aguardandoAssinatura ?? 0}
          label="Aguardando assinatura"
          color="var(--verta-amber)"
        />
        <StatCard value={stats?.assinadosEsteMes ?? 0} label="Assinados este mês" color="var(--verta-green)" />
        <StatCard
          value={stats?.templatesCadastrados ?? 0}
          label="Templates cadastrados"
          color="var(--verta-gray)"
        />
      </div>

      <div className="dashboard-grid">
        <div className="card panel">
          <div className="panel-title">Contratos recentes</div>
          {contratosRecentes.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-title">Nenhum contrato ainda</div>
              Gere seu primeiro contrato a partir de um template.
            </div>
          ) : (
            contratosRecentes.map((contrato) => (
              <div className="recent-row" key={contrato.id}>
                <div className="recent-row-name">{contrato.titulo}</div>
                <div className="recent-row-meta">
                  <span className="recent-row-date">
                    {contrato.dataAtualizacao
                      ? new Date(contrato.dataAtualizacao).toLocaleDateString('pt-BR')
                      : '-'}
                  </span>
                  <StatusBadge status={contrato.status} />
                </div>
              </div>
            ))
          )}
        </div>

        <div className="card quick-actions">
          <div className="panel-title">Ações rápidas</div>
          <button className="quick-action-btn" onClick={irParaNovoContrato}>
            + Novo contrato a partir de template
          </button>
          <button className="quick-action-btn" onClick={irParaUploadTemplate}>
            ↑ Upload de template
          </button>
          <button className="quick-action-btn" onClick={irParaAguardandoAssinatura}>
            ✓ Ir para assinaturas pendentes
          </button>
          <button className="quick-action-btn" onClick={() => navigate('/planos')}>
            $ Ver planos e cobrança
          </button>
        </div>
      </div>
    </div>
  )
}
