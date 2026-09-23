import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { contratosApi } from '../api/contratos'
import { templatesApi } from '../api/templates'
import { versoesContratoApi } from '../api/versoesContrato'
import { arquivosApi } from '../api/arquivos'
import { StatusBadge } from '../components/StatusBadge'
import { TemplateEditor, type TemplateEditorHandle } from '../components/TemplateEditor'
import { useAuth } from '../context/AuthContext'
import { converterMarkupAntigoParaHtml, pareceHtml } from '../utils/formatarConteudo'
import { baixarContratoPdf } from '../utils/pdf'
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
  const isAdmin = usuario?.perfil === 'ADMIN'
  const [pasta, setPasta] = useState<Pasta>('meus-contratos')
  const [contratos, setContratos] = useState<Contrato[]>([])
  const [templates, setTemplates] = useState<Template[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [mostrarUpload, setMostrarUpload] = useState(false)
  const [templateEditando, setTemplateEditando] = useState<Template | null>(null)
  const [selecionados, setSelecionados] = useState<Set<number>>(new Set())

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

  useEffect(() => {
    setSelecionados(new Set())
  }, [pasta])

  const statusFiltro = STATUS_POR_PASTA[pasta]
  const contratosFiltrados = statusFiltro
    ? contratos.filter((c) => statusFiltro.includes(c.status))
    : contratos

  const ehPastaDeContratos = pasta !== 'templates'

  function recarregarTemplates() {
    if (!usuario) return
    templatesApi.listar(usuario.empresaId).then(setTemplates)
  }

  function recarregarContratos() {
    if (!usuario) return
    contratosApi.listar(usuario.empresaId).then(setContratos)
  }

  async function excluirTemplate(template: Template) {
    if (!template.id) return
    if (!window.confirm(`Excluir o template "${template.nome}"? Essa ação não pode ser desfeita.`)) return
    try {
      await templatesApi.remover(template.id)
      recarregarTemplates()
    } catch {
      setErro('Não foi possível excluir o template.')
    }
  }

  async function excluirContrato(contrato: Contrato) {
    if (!contrato.id) return
    if (!window.confirm(`Excluir o contrato "${contrato.titulo}"? Essa ação não pode ser desfeita.`)) return
    try {
      await contratosApi.remover(contrato.id)
      recarregarContratos()
    } catch {
      setErro('Não foi possível excluir o contrato. Verifique se você tem permissão.')
    }
  }

  function alternarSelecao(id: number) {
    setSelecionados((atual) => {
      const novo = new Set(atual)
      if (novo.has(id)) novo.delete(id)
      else novo.add(id)
      return novo
    })
  }

  async function excluirSelecionados() {
    if (selecionados.size === 0) return
    if (
      !window.confirm(
        `Excluir ${selecionados.size} contrato(s) selecionado(s)? Essa ação não pode ser desfeita.`
      )
    ) {
      return
    }
    try {
      await Promise.all(Array.from(selecionados).map((id) => contratosApi.remover(id)))
      setSelecionados(new Set())
      recarregarContratos()
    } catch {
      setErro('Não foi possível excluir todos os contratos selecionados. Verifique suas permissões.')
      recarregarContratos()
    }
  }

  return (
    <div className="files-layout">
      <div className="card files-main">
        <div className="files-toolbar">
          {pasta === 'templates' && (
            <button
              className="btn btn-secondary"
              onClick={() => {
                setTemplateEditando(null)
                setMostrarUpload((v) => !v)
              }}
            >
              + Novo template
            </button>
          )}

          {ehPastaDeContratos && (
            <>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  setTemplateEditando(null)
                  setMostrarUpload((v) => !v)
                }}
              >
                ↑ Upload
              </button>
              {selecionados.size > 0 && (
                <button className="btn btn-secondary" onClick={excluirSelecionados}>
                  🗑 Excluir selecionados ({selecionados.size})
                </button>
              )}
              {pasta === 'meus-contratos' && (
                <button className="btn btn-primary" onClick={() => navigate('/contratos/gerar')}>
                  + Novo
                </button>
              )}
            </>
          )}
        </div>

        {templateEditando && (
          <NovoTemplateForm
            key={templateEditando.id}
            templateExistente={templateEditando}
            onConcluido={() => {
              setTemplateEditando(null)
              recarregarTemplates()
            }}
          />
        )}

        {mostrarUpload && !templateEditando && pasta === 'templates' && (
          <NovoTemplateForm
            onConcluido={() => {
              setMostrarUpload(false)
              recarregarTemplates()
            }}
          />
        )}

        {mostrarUpload && ehPastaDeContratos && (
          <UploadArquivoForm
            contratos={contratos}
            onConcluido={() => setMostrarUpload(false)}
          />
        )}

        {erro && <div className="status-message error">{erro}</div>}

        {carregando ? (
          <div className="loading-text">Carregando...</div>
        ) : pasta === 'templates' ? (
          <ListaTemplates templates={templates} onEditar={setTemplateEditando} onExcluir={excluirTemplate} />
        ) : (
          <ListaContratos
            contratos={contratosFiltrados}
            usuarioId={usuario?.usuarioId}
            isAdmin={isAdmin}
            selecionados={selecionados}
            onAlternarSelecao={alternarSelecao}
            onAbrir={(c) => navigate(`/contratos/${c.id}`)}
            onExcluir={excluirContrato}
          />
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

function ListaContratos({
  contratos,
  usuarioId,
  isAdmin,
  selecionados,
  onAlternarSelecao,
  onAbrir,
  onExcluir
}: {
  contratos: Contrato[]
  usuarioId?: number
  isAdmin: boolean
  selecionados: Set<number>
  onAlternarSelecao: (id: number) => void
  onAbrir: (contrato: Contrato) => void
  onExcluir: (contrato: Contrato) => void
}) {
  if (contratos.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-title">Nada por aqui ainda</div>
        Os contratos que você criar ou que compartilharem com você vão aparecer nesta pasta.
      </div>
    )
  }

  return (
    <div>
      {contratos.map((contrato) => {
        // Contrato assinado (FINALIZADO) nunca pode ser excluido por aqui - so arquivado/cancelado.
        // Poder de excluir: quem criou o contrato, contas ADMIN, ou quem recebeu permissao de excluir
        // via compartilhamento (essa ultima so e reconhecida dentro do proprio contrato, em /contratos/:id).
        const podeExcluir =
          contrato.status !== 'FINALIZADO' && (isAdmin || contrato.criadoPor === usuarioId)

        return (
          <div className="file-row" key={contrato.id} onClick={() => onAbrir(contrato)} style={{ cursor: 'pointer' }}>
            <div className="file-row-left">
              {podeExcluir && (
                <input
                  type="checkbox"
                  checked={selecionados.has(contrato.id!)}
                  onClick={(e) => e.stopPropagation()}
                  onChange={() => onAlternarSelecao(contrato.id!)}
                />
              )}
              <div className="file-icon">📄</div>
              <div>
                <div className="file-name">{contrato.titulo}</div>
                <div className="file-sub">{contrato.tipo ?? 'Contrato'}</div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
              <StatusBadge status={contrato.status} />
              {podeExcluir && (
                <button
                  className="btn btn-secondary"
                  onClick={(e) => {
                    e.stopPropagation()
                    onExcluir(contrato)
                  }}
                >
                  🗑 Excluir
                </button>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}

function ListaTemplates({
  templates,
  onEditar,
  onExcluir
}: {
  templates: Template[]
  onEditar: (template: Template) => void
  onExcluir: (template: Template) => void
}) {
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
          <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
            <span className={`badge ${template.ativo ? 'badge-finalizado' : 'badge-arquivado'}`}>
              {template.ativo ? 'Ativo' : 'Inativo'}
            </span>
            <button className="btn btn-secondary" onClick={() => onEditar(template)}>
              ✎ Editar
            </button>
            <button className="btn btn-secondary" onClick={() => onExcluir(template)}>
              🗑 Excluir
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}

function NovoTemplateForm({
  templateExistente,
  onConcluido
}: {
  templateExistente?: Template
  onConcluido: () => void
}) {
  const { usuario } = useAuth()
  const [nome, setNome] = useState(templateExistente?.nome ?? '')
  const [descricao, setDescricao] = useState(templateExistente?.descricao ?? '')
  const conteudoOriginal = templateExistente?.conteudo ?? ''
  const [conteudo, setConteudo] = useState(
    conteudoOriginal && !pareceHtml(conteudoOriginal) ? converterMarkupAntigoParaHtml(conteudoOriginal) : conteudoOriginal
  )
  const [ativo, setAtivo] = useState(templateExistente?.ativo ?? true)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [descricaoIa, setDescricaoIa] = useState('')
  const [gerandoIa, setGerandoIa] = useState(false)
  const [erroIa, setErroIa] = useState<string | null>(null)
  const editorRef = useRef<TemplateEditorHandle>(null)

  async function gerarComIa() {
    if (!descricaoIa.trim()) return
    setGerandoIa(true)
    setErroIa(null)
    try {
      const resultado = await templatesApi.gerarComIa(descricaoIa.trim())
      const html = converterMarkupAntigoParaHtml(resultado.conteudo)
      setConteudo(html)
      editorRef.current?.setContent(html)
      if (!nome.trim()) setNome(resultado.nome)
    } catch (e) {
      const mensagem =
        (e as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Não foi possível gerar o template com IA.'
      setErroIa(mensagem)
    } finally {
      setGerandoIa(false)
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!usuario) return
    if (!conteudo || conteudo === '<p></p>') {
      setErro('Escreva o conteúdo do template.')
      return
    }
    setEnviando(true)
    setErro(null)
    try {
      if (templateExistente?.id) {
        await templatesApi.atualizar(templateExistente.id, {
          empresaId: usuario.empresaId,
          nome,
          descricao: descricao || null,
          conteudo,
          ativo
        })
      } else {
        await templatesApi.criar({
          empresaId: usuario.empresaId,
          nome,
          descricao: descricao || null,
          conteudo,
          ativo: true
        })
      }
      onConcluido()
    } catch (e) {
      const mensagem =
        (e as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Não foi possível salvar o template.'
      setErro(mensagem)
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
          value={descricao ?? ''}
          onChange={(e) => setDescricao(e.target.value)}
          placeholder="Opcional"
        />
      </div>
      <div className="field">
        <label>Gerar conteúdo com IA</label>
        {erroIa && <div className="status-message error">{erroIa}</div>}
        <div style={{ display: 'flex', gap: 10 }}>
          <input
            value={descricaoIa}
            onChange={(e) => setDescricaoIa(e.target.value)}
            placeholder="Descreva o contrato, ex: prestação de serviços de consultoria de TI"
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault()
                gerarComIa()
              }
            }}
          />
          <button
            type="button"
            className="btn btn-secondary"
            onClick={gerarComIa}
            disabled={gerandoIa || !descricaoIa.trim()}
          >
            {gerandoIa ? 'Gerando...' : '✨ Gerar com IA'}
          </button>
        </div>
        <div className="form-panel-hint" style={{ marginTop: 6, marginBottom: 0 }}>
          A IA preenche o conteúdo abaixo (e o nome, se estiver vazio). Revise antes de salvar.
        </div>
      </div>
      <div className="field">
        <label>Conteúdo do template</label>
        <TemplateEditor ref={editorRef} conteudoInicial={conteudo} onChange={setConteudo} />
        <div className="form-panel-hint" style={{ marginTop: 6, marginBottom: 0 }}>
          Use <code>{'{{variavel}}'}</code> para os campos que serão preenchidos na geração do contrato.
        </div>
      </div>
      {templateExistente && (
        <div className="field">
          <label>
            <input type="checkbox" checked={ativo} onChange={(e) => setAtivo(e.target.checked)} /> Ativo (aparece na
            geração de contrato)
          </label>
        </div>
      )}
      <div style={{ display: 'flex', gap: 10 }}>
        {templateExistente && (
          <button type="button" className="btn btn-secondary" onClick={onConcluido}>
            Cancelar
          </button>
        )}
        <button type="submit" className="btn btn-primary" disabled={enviando}>
          {enviando ? 'Salvando...' : templateExistente ? 'Salvar alterações' : 'Cadastrar template'}
        </button>
      </div>
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
      const nome = nomeArquivo.trim()
      // Nao ha armazenamento em servidor: o "upload" baixa o PDF direto no computador
      // de quem esta cadastrando, e so o registro (nome/versao) fica salvo no banco.
      baixarContratoPdf(nome, ultimaVersao.conteudo)
      await arquivosApi.criar({
        contratoId: Number(contratoId),
        versaoId: ultimaVersao.id,
        nomeArquivo: nome,
        caminhoArquivo: `${nome}.pdf`,
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
          placeholder="contrato_assinado"
          required
        />
      </div>
      <div className="form-panel-hint" style={{ marginTop: -6 }}>
        Ao enviar, o PDF do contrato é baixado automaticamente no seu computador.
      </div>
      <button type="submit" className="btn btn-primary" disabled={enviando}>
        {enviando ? 'Gerando PDF...' : '⬇ Upload'}
      </button>
    </form>
  )
}
