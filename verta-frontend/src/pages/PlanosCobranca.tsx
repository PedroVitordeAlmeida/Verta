import { useEffect, useState } from 'react'
import { empresasApi } from '../api/empresas'
import { planosApi } from '../api/planos'
import { templatesApi } from '../api/templates'
import { usuariosApi } from '../api/usuarios'
import { useAuth } from '../context/AuthContext'
import type { Empresa, PeriodoPlano, Plano } from '../types'

const NOME_PLANO: Record<string, string> = {
  BASICO: 'Básico',
  PLUS: 'Plus'
}

const LABEL_PERIODO: Record<PeriodoPlano, string> = {
  SEIS_MESES: '6 meses',
  UM_ANO: '1 ano',
  TRES_ANOS: '3 anos'
}

function formatarData(iso?: string | null) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return '—'
  return data.toLocaleDateString('pt-BR')
}

export function PlanosCobranca() {
  const { usuario } = useAuth()
  const ehSuperAdmin = usuario?.perfil === 'SUPERADMIN'

  return (
    <div>{ehSuperAdmin ? <PainelSuperAdmin /> : <PainelDaEmpresa />}</div>
  )
}

/** Visao da equipe Verta: lista todas as empresas clientes e atribui/troca o plano de cada uma. */
function PainelSuperAdmin() {
  const [empresas, setEmpresas] = useState<Empresa[]>([])
  const [planos, setPlanos] = useState<Plano[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  function carregar() {
    setCarregando(true)
    setErro(null)
    Promise.all([empresasApi.listar(), planosApi.listar()])
      .then(([empresasResp, planosResp]) => {
        setEmpresas(empresasResp)
        setPlanos(planosResp)
      })
      .catch(() => setErro('Não foi possível carregar as empresas/planos.'))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [])

  return (
    <div className="card panel">
      <div className="panel-title">Planos das empresas clientes</div>
      {erro && <div className="status-message error">{erro}</div>}
      {carregando ? (
        <div className="loading-text">Carregando...</div>
      ) : empresas.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-title">Nenhuma empresa cadastrada</div>
        </div>
      ) : (
        empresas.map((empresa) => (
          <LinhaEmpresa key={empresa.id} empresa={empresa} planos={planos} onAtribuido={carregar} />
        ))
      )}
    </div>
  )
}

function LinhaEmpresa({
  empresa,
  planos,
  onAtribuido
}: {
  empresa: Empresa
  planos: Plano[]
  onAtribuido: () => void
}) {
  const planoAtual = planos.find((p) => p.id === empresa.planoId)
  const [planoId, setPlanoId] = useState<number | ''>(empresa.planoId ?? '')
  const [periodo, setPeriodo] = useState<PeriodoPlano>((empresa.periodoPlano as PeriodoPlano) ?? 'UM_ANO')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function atribuir() {
    if (!empresa.id || !planoId) return
    setEnviando(true)
    setErro(null)
    try {
      await empresasApi.atribuirPlano(empresa.id, Number(planoId), periodo)
      onAtribuido()
    } catch {
      setErro('Não foi possível atribuir o plano.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="recent-row" style={{ flexWrap: 'wrap', gap: 10 }}>
      <div>
        <div className="recent-row-name">{empresa.nome}</div>
        <div className="file-sub">
          {planoAtual ? (
            <>
              Plano <strong>{NOME_PLANO[planoAtual.nome] ?? planoAtual.nome}</strong>
              {empresa.periodoPlano && ` · ${LABEL_PERIODO[empresa.periodoPlano as PeriodoPlano] ?? empresa.periodoPlano}`}
              {' · '}vence em {formatarData(empresa.dataFimPlano)}
            </>
          ) : (
            'Sem plano atribuído'
          )}
        </div>
        {erro && <div className="status-message error" style={{ marginTop: 6 }}>{erro}</div>}
      </div>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        <select value={planoId} onChange={(e) => setPlanoId(Number(e.target.value))}>
          <option value="" disabled>
            Selecione o plano
          </option>
          {planos.map((p) => (
            <option key={p.id} value={p.id}>
              {NOME_PLANO[p.nome] ?? p.nome}
            </option>
          ))}
        </select>
        <select value={periodo} onChange={(e) => setPeriodo(e.target.value as PeriodoPlano)}>
          <option value="SEIS_MESES">6 meses</option>
          <option value="UM_ANO">1 ano</option>
          <option value="TRES_ANOS">3 anos</option>
        </select>
        <button className="btn btn-primary" onClick={atribuir} disabled={enviando || !planoId}>
          {enviando ? 'Salvando...' : planoAtual ? 'Trocar plano' : 'Atribuir plano'}
        </button>
      </div>
    </div>
  )
}

/** Visao da propria empresa cliente: plano atual, vigencia e uso dos limites contratados. */
function PainelDaEmpresa() {
  const { usuario } = useAuth()
  const [empresa, setEmpresa] = useState<Empresa | null>(null)
  const [planos, setPlanos] = useState<Plano[]>([])
  const [totalUsuarios, setTotalUsuarios] = useState(0)
  const [totalTemplatesCriados, setTotalTemplatesCriados] = useState(0)
  const [totalTemplatesBase, setTotalTemplatesBase] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (!usuario) return
    setCarregando(true)
    setErro(null)
    Promise.all([
      empresasApi.buscar(usuario.empresaId),
      planosApi.listar(),
      usuariosApi.listar(usuario.empresaId),
      templatesApi.listar(usuario.empresaId)
    ])
      .then(([empresaResp, planosResp, usuariosResp, templatesResp]) => {
        setEmpresa(empresaResp)
        setPlanos(planosResp)
        setTotalUsuarios(usuariosResp.length)
        setTotalTemplatesCriados(templatesResp.filter((t) => !t.origemSistema).length)
        setTotalTemplatesBase(templatesResp.filter((t) => t.origemSistema).length)
      })
      .catch(() => setErro('Não foi possível carregar as informações do plano.'))
      .finally(() => setCarregando(false))
  }, [usuario])

  if (carregando) return <div className="card panel loading-text">Carregando...</div>
  if (erro) return <div className="card panel status-message error">{erro}</div>

  const plano = planos.find((p) => p.id === empresa?.planoId)

  if (!empresa || !plano) {
    return (
      <div className="card panel">
        <div className="panel-title">Plano e cobrança</div>
        <div className="empty-state">
          <div className="empty-state-title">Nenhum plano atribuído ainda</div>
          Fale com a equipe Verta para contratar um plano para a sua empresa.
        </div>
      </div>
    )
  }

  return (
    <div className="card panel">
      <div className="panel-title">Plano e cobrança</div>

      <div className="recent-row">
        <div>
          <div className="recent-row-name">Plano {NOME_PLANO[plano.nome] ?? plano.nome}</div>
          <div className="file-sub">
            {empresa.periodoPlano && (LABEL_PERIODO[empresa.periodoPlano as PeriodoPlano] ?? empresa.periodoPlano)}
            {' · '}vigente até {formatarData(empresa.dataFimPlano)}
          </div>
        </div>
        <span className="badge badge-finalizado">
          {plano.validacaoJuridicaBase ? 'Templates com validação jurídica' : 'Templates sem validação jurídica'}
        </span>
      </div>

      <div className="recent-row">
        <div>
          <div className="recent-row-name">Usuários</div>
          <div className="file-sub">{totalUsuarios} de {plano.maxUsuarios} usuários utilizados</div>
        </div>
      </div>

      <div className="recent-row">
        <div>
          <div className="recent-row-name">Templates criados pela empresa</div>
          <div className="file-sub">{totalTemplatesCriados} de {plano.maxTemplates} templates utilizados</div>
        </div>
      </div>

      <div className="recent-row">
        <div>
          <div className="recent-row-name">Templates base disponibilizados pela Verta</div>
          <div className="file-sub">{totalTemplatesBase} templates (não contam no limite acima)</div>
        </div>
      </div>

      {plano.quotaGeracaoJuridicaMensal != null && (
        <div className="recent-row">
          <div>
            <div className="recent-row-name">Validação jurídica mensal</div>
            <div className="file-sub">
              Até {plano.quotaGeracaoJuridicaMensal} templates por mês com validação da nossa equipe jurídica
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
