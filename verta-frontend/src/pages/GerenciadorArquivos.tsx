import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { contratosApi } from '../api/contratos'
import { templatesApi } from '../api/templates'
import { versoesContratoApi } from '../api/versoesContrato'
import { arquivosApi } from '../api/arquivos'
import { StatusBadge } from '../components/StatusBadge'
import { useAuth } from '../context/AuthContext'
import type { Contrato, StatusContrato, Template, VersaoContrato } from '../types'

type Pasta = 'meus-contratos' | 'templates' | 'contratos-gerados' | 'contratos-assinados' | 'arquivados'

const PASTAS: { id: Pasta; label: string }[] = [
  { id: 'meus-contratos', label: 'Meus Contratos' },
  { id: 'templates', label: 'Templates' },
  { id: 'contratos-gerados', label: 'Contratos Gerados' },
  { id: 'contratos-assinados', label: 'Contratos Assinados' },
  { id: 'arquivados', label: 'Arquivados' }
]

const STATUS_POR_PASTA: Record<Pasta, StatusContrato[] | null> = {
  'meus-contratos': null,
  templates: null,
  'contratos-gerados': ['RASCUNHO', 'EM_REVISAO'],
  'contratos-assinados': ['FINALIZADO'],
  arquivados: ['ARQUIVADO', 'CANCELADO']
}

export function GerenciadorArquivos() {
  const { usuario } = useAuth()
  const navigate = useNavigate()
  const [pasta, setPasta] = useState<Pasta>('meus-contratos')
  const [contratos, setContratos] = useState<Contrato[]>([])
  const [templates, setTemplates] = useState<Template[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [mostrarUpload, setMostrarUpload] = useState(false)

  useEffect(() => {
    if (!usuario) return
    let ativo = true
    setCarregando(true)
    setErro(null)

    Promise.all([contratosApi.listar(usuario.empresaId), templatesApi.listar(usuario.empresaId)])
      .then(([contratosResp, templatesResp]) => {
        if (!ativo) return
        setContratos(contratosResp)
        setTemplates(templatesResp)
      })
      .catch(() => ativo && setErro('Não foi possível carregar os arquivos.'))
      .finally(() => ativo && setCarregando(false))

    return () => {
      ativo = false
    }
  }, [usuario])

  const statusFiltro = STATUS_POR_PASTA[pasta]
  const contratosFiltrados = statusFiltro
    ? contratos.filter((c) => statusFiltro.includes(c.status))
    : contratos

  function recarregarTemplates() {
    if (!usuario) return
    templatesApi.listar(usuario.empresaId).then(setTemplates)
  }

  return (
    <div className="files-layout">
      <div className="card files-main">
        <div className="files-toolbar">
          <button className="btn btn-secondary" onClick={() => setMostrarUpload((v) => !v)}>
            {pasta === 'templates' ? '+ Novo template' : '↑ Upload'}
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/contratos/gerar')}>
            + Novo
          </button>
        </div>

        {mostrarUpload && pasta === 'templates' && (
          <NovoTemplateForm
            onConcluido={() => {
              setMostrarUpload(false)
              recarregarTemplates()
            }}
          />
        )}

        {mostrarUpload && pasta !== 'templates' && (
          <UploadArquivoForm
            contratos={contratos}
            onConcluido={() => setMostrarUpload(false)}
          />
        )}

        {erro && <div className="status-message error">{erro}</div>}

        {carregando ? (
          <div className="loading-text">Carregando...</div>
        ) : pasta === 'templates' ? (
          <ListaTemplates templates={templates} />
        ) : (
          <ListaContratos contratos={contratosFiltrados} />
        )}
      </div>

      <div className="card folder-list">
        {PASTAS.map((p) => (
          <div
            key={p.id}
            className={`folder-item${pasta === p.id ? ' active' : ''}`}
            onClick={() => setPasta(p.id)}
          >
            📁 {p.label}
          </div>
        ))}
      </div>
    </div>
  )
}

function ListaContratos({ contratos }: { contratos: Contrato[] }) {
  if (contratos.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-title">Nada por aqui ainda</div>
        Os contratos que você criar vão aparecer nesta pasta.
      </div>
    )
  }

  return (
    <div>
      {contratos.map((contrato) => (
        <div className="file-row" key={contrato.id}>
          <div className="file-row-left">
            <div className="file-icon">📄</div>
            <div>
              <div className="file-name">{contrato.titulo}</div>
              <div className="file-sub">{contrato.tipo ?? 'Contrato'}</div>
            </div>
          </div>
          <StatusBadge status={contrato.status} />
        </div>
      ))}
    </div>
  )
}

function ListaTemplates({ templates }: { templates: Template[] }) {
  if (templates.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-title">Nenhum template cadastrado</div>
        Cadastre um template para poder gerar contratos a partir dele.
      </div>
    )
  }

  return (
    <div>
      {templates.map((template) => (
        <div className="file-row" key={template.id}>
          <div className="file-row-left">
            <div className="file-icon">📝</div>
            <div>
              <div className="file-name">{template.nome}</div>
              <div className="file-sub">{template.descricao ?? 'Sem descrição'}</div>
            </div>
          </div>
          <span className={`badge ${template.ativo ? 'badge-finalizado' : 'badge-arquivado'}`}>
            {template.ativo ? 'Ativo' : 'Inativo'}
          </span>
        </div>
      ))}
    </div>
  )
}

function NovoTemplateForm({ onConcluido }: { onConcluido: () => void }) {
  const { usuario } = useAuth()
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [conteudo, setConteudo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!usuario) return
    setEnviando(true)
    setErro(null)
    try {
      await templatesApi.criar({
        empresaId: usuario.empresaId,
        nome,
        descricao: descricao || null,
        conteudo,
        ativo: true
      })
      onConcluido()
    } catch {
      setErro('Não foi possível cadastrar o template.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="card" style={{ padding: 18, marginBottom: 18 }} onSubmit={handleSubmit}>
      {erro && <div className="status-message error">{erro}</div>}
      <div className="field">
        <label>Nome do template</label>
        <input
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          placeholder="Contrato de Prestação de Serviços"
          required
        />
      </div>
      <div className="field">
        <label>Descrição</label>
        <input
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          placeholder="Opcional"
        />
      </div>
      <div className="field">
        <label>Conteúdo do template</label>
        <textarea
          value={conteudo}
          onChange={(e) => setConteudo(e.target.value)}
          placeholder="Use {{variavel}} para os campos que serão preenchidos na geração do contrato."
          rows={8}
          required
        />
      </div>
      <button type="submit" className="btn btn-primary" disabled={enviando}>
        {enviando ? 'Cadastrando...' : 'Cadastrar template'}
      </button>
    </form>
  )
}

function UploadArquivoForm({
  contratos,
  onConcluido
}: {
  contratos: Contrato[]
  onConcluido: () => void
}) {
  const { usuario } = useAuth()
  const [contratoId, setContratoId] = useState<number | ''>('')
  const [nomeArquivo, setNomeArquivo] = useState('')
  const [caminhoArquivo, setCaminhoArquivo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!contratoId || !usuario) return
    setEnviando(true)
    setErro(null)
    try {
      const versoes: VersaoContrato[] = await versoesContratoApi.listarPorContrato(Number(contratoId))
      const ultimaVersao = versoes[0]
      if (!ultimaVersao?.id) {
        setErro('Esse contrato ainda não tem nenhuma versão gerada.')
        return
      }
      await arquivosApi.criar({
        contratoId: Number(contratoId),
        versaoId: ultimaVersao.id,
        nomeArquivo,
        caminhoArquivo,
        tipoArquivo: 'application/pdf',
        tamanho: null
      })
      onConcluido()
    } catch {
      setErro('Não foi possível registrar o arquivo.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="card" style={{ padding: 18, marginBottom: 18 }} onSubmit={handleSubmit}>
      {erro && <div className="status-message error">{erro}</div>}
      <div className="field">
        <label>Contrato</label>
        <select value={contratoId} onChange={(e) => setContratoId(Number(e.target.value))} required>
          <option value="" disabled>
            Selecione um contrato
          </option>
          {contratos.map((c) => (
            <option key={c.id} value={c.id}>
              {c.titulo}
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <label>Nome do arquivo</label>
        <input
          value={nomeArquivo}
          onChange={(e) => setNomeArquivo(e.target.value)}
          placeholder="contrato_assinado.pdf"
          required
        />
      </div>
      <div className="field">
        <label>Caminho / URL de armazenamento</label>
        <input
          value={caminhoArquivo}
          onChange={(e) => setCaminhoArquivo(e.target.value)}
          placeholder="/documentos/contratos/1/contrato.pdf"
          required
        />
      </div>
      <button type="submit" className="btn btn-primary" disabled={enviando}>
        {enviando ? 'Enviando...' : 'Registrar arquivo'}
      </button>
    </form>
  )
}
