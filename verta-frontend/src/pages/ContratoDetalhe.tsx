import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { contratosApi } from '../api/contratos'
import { contratoUsuariosApi } from '../api/contratoUsuarios'
import { versoesContratoApi } from '../api/versoesContrato'
import { usuariosApi } from '../api/usuarios'
import { StatusBadge } from '../components/StatusBadge'
import { useAuth } from '../context/AuthContext'
import type { Contrato, ContratoUsuario, StatusContrato, Usuario, VersaoContrato } from '../types'

const TRANSICOES: Record<StatusContrato, { proximo: StatusContrato; rotulo: string }[]> = {
  RASCUNHO: [{ proximo: 'EM_REVISAO', rotulo: 'Enviar para revisão' }],
  EM_REVISAO: [
    { proximo: 'RASCUNHO', rotulo: 'Voltar para rascunho' },
    { proximo: 'FINALIZADO', rotulo: 'Marcar como assinado' }
  ],
  FINALIZADO: [{ proximo: 'ARQUIVADO', rotulo: 'Arquivar' }],
  ARQUIVADO: [],
  CANCELADO: []
}

export function ContratoDetalhe() {
  const { id } = useParams()
  const contratoId = Number(id)
  const { usuario } = useAuth()
  const navigate = useNavigate()

  const [contrato, setContrato] = useState<Contrato | null>(null)
  const [versoes, setVersoes] = useState<VersaoContrato[]>([])
  const [compartilhamentos, setCompartilhamentos] = useState<ContratoUsuario[]>([])
  const [usuariosEmpresa, setUsuariosEmpresa] = useState<Usuario[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [naoEncontrado, setNaoEncontrado] = useState(false)

  const [editando, setEditando] = useState(false)
  const [conteudoEditado, setConteudoEditado] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [mudandoStatus, setMudandoStatus] = useState(false)
  const [mostrarCompartilhar, setMostrarCompartilhar] = useState(false)

  function carregarTudo() {
    if (!usuario || !contratoId) return
    setCarregando(true)
    setErro(null)
    setNaoEncontrado(false)

    contratosApi
      .buscar(contratoId)
      .then((c) => {
        setContrato(c)
        return versoesContratoApi.listarPorContrato(contratoId)
      })
      .then((v) => setVersoes(v))
      .catch((e) => {
        if (e?.response?.status === 404) setNaoEncontrado(true)
        else setErro('Não foi possível carregar o contrato.')
      })
      .finally(() => setCarregando(false))
  }

  useEffect(carregarTudo, [contratoId, usuario])

  const souDono = !!contrato && !!usuario && contrato.criadoPor === usuario.usuarioId
  const souAdmin = usuario?.perfil === 'ADMIN'
  const meuVinculo = compartilhamentos.find((c) => c.usuarioId === usuario?.usuarioId)
  const podeEditarConteudo = souDono || souAdmin || !!meuVinculo?.podeEditar
  const podeMudarStatus = souDono || souAdmin || !!meuVinculo?.podeEditar || !!meuVinculo?.podeAssinar
  const podeCompartilhar = souDono || souAdmin
  const statusEditavel = contrato?.status === 'RASCUNHO' || contrato?.status === 'EM_REVISAO'

  const ultimaVersao = versoes[0]

  // Busca os vinculos mesmo para quem so recebeu compartilhamento (nao e dono/ADMIN),
  // para que o proprio vinculo (meuVinculo) reflita corretamente editar/assinar/excluir.
  useEffect(() => {
    if (!contrato) return
    contratoUsuariosApi.listarPorContrato(contratoId).then(setCompartilhamentos).catch(() => {})
  }, [contrato, contratoId])

  useEffect(() => {
    if (!podeCompartilhar) return
    usuariosApi.listar().then(setUsuariosEmpresa)
  }, [podeCompartilhar])

  function iniciarEdicao() {
    setConteudoEditado(ultimaVersao?.conteudo ?? '')
    setEditando(true)
  }

  async function salvarEdicao() {
    if (!usuario) return
    setSalvando(true)
    setErro(null)
    try {
      await versoesContratoApi.criar({
        contratoId,
        conteudo: conteudoEditado,
        criadoPor: usuario.usuarioId
      })
      setEditando(false)
      carregarTudo()
    } catch {
      setErro('Não foi possível salvar as alterações.')
    } finally {
      setSalvando(false)
    }
  }

  async function alterarStatus(novoStatus: StatusContrato) {
    setMudandoStatus(true)
    setErro(null)
    try {
      await contratosApi.atualizarStatus(contratoId, novoStatus)
      carregarTudo()
    } catch {
      setErro('Não foi possível alterar o status do contrato.')
    } finally {
      setMudandoStatus(false)
    }
  }

  if (carregando) {
    return <div className="loading-text">Carregando contrato...</div>
  }

  if (naoEncontrado || !contrato) {
    return (
      <div className="card">
        <div className="empty-state">
          <div className="empty-state-title">Contrato não encontrado</div>
          Ele pode ter sido removido ou você não tem acesso a ele.
        </div>
        <button className="btn btn-secondary" style={{ marginTop: 14 }} onClick={() => navigate('/arquivos')}>
          Voltar para Gerenciador de Arquivos
        </button>
      </div>
    )
  }

  return (
    <div className="contract-layout">
      <div className="card">
        <div className="files-toolbar" style={{ justifyContent: 'space-between' }}>
          <div>
            <div className="form-panel-title">{contrato.titulo}</div>
            <StatusBadge status={contrato.status} />
          </div>
          {statusEditavel && podeEditarConteudo && !editando && (
            <button className="btn btn-secondary" onClick={iniciarEdicao}>
              ✎ Editar conteúdo
            </button>
          )}
        </div>

        {erro && <div className="status-message error">{erro}</div>}

        {editando ? (
          <>
            <textarea
              value={conteudoEditado}
              onChange={(e) => setConteudoEditado(e.target.value)}
              rows={18}
              style={{ width: '100%', marginTop: 12 }}
            />
            <div style={{ display: 'flex', gap: 10, marginTop: 10 }}>
              <button className="btn btn-secondary" onClick={() => setEditando(false)} disabled={salvando}>
                Cancelar
              </button>
              <button className="btn btn-primary" onClick={salvarEdicao} disabled={salvando}>
                {salvando ? 'Salvando...' : 'Salvar alterações'}
              </button>
            </div>
          </>
        ) : (
          <div className="editor-body">
            {ultimaVersao ? (
              ultimaVersao.conteudo.split('\n').map((linha, i) => <p key={i}>{linha}</p>)
            ) : (
              <span className="empty-state-title">Esse contrato ainda não tem conteúdo gerado.</span>
            )}
          </div>
        )}
      </div>

      <div className="card form-panel">
        <div className="form-panel-title">Status</div>
        <div className="form-panel-hint">
          {podeMudarStatus
            ? 'Mudar o status move o contrato entre as pastas do Gerenciador de Arquivos.'
            : 'Você só pode visualizar este contrato.'}
        </div>
        {podeMudarStatus && TRANSICOES[contrato.status].length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {TRANSICOES[contrato.status].map((t) => (
              <button
                key={t.proximo}
                className="btn btn-secondary"
                disabled={mudandoStatus}
                onClick={() => alterarStatus(t.proximo)}
              >
                {t.rotulo}
              </button>
            ))}
          </div>
        )}

        {podeCompartilhar && (
          <>
            <div className="section-title">Compartilhamento</div>
            {compartilhamentos.length === 0 ? (
              <div className="empty-state">Ninguém mais tem acesso a este contrato ainda.</div>
            ) : (
              compartilhamentos.map((c) => {
                const nome = usuariosEmpresa.find((u) => u.id === c.usuarioId)?.nome ?? `Usuário #${c.usuarioId}`
                return (
                  <div className="file-row" key={c.id}>
                    <div className="file-row-left">
                      <div className="file-icon">👤</div>
                      <div>
                        <div className="file-name">{nome}</div>
                        <div className="file-sub">
                          {[
                            c.podeVisualizar && 'ver',
                            c.podeEditar && 'editar',
                            c.podeAssinar && 'assinar',
                            c.podeExcluir && 'excluir'
                          ]
                            .filter(Boolean)
                            .join(', ')}
                        </div>
                      </div>
                    </div>
                    <button
                      className="btn btn-secondary"
                      onClick={() =>
                        contratoUsuariosApi
                          .remover(c.id!)
                          .then(() => setCompartilhamentos((atual) => atual.filter((x) => x.id !== c.id)))
                      }
                    >
                      Remover
                    </button>
                  </div>
                )
              })
            )}

            {mostrarCompartilhar ? (
              <FormularioCompartilhar
                contratoId={contratoId}
                usuariosDisponiveis={usuariosEmpresa.filter(
                  (u) => u.id !== contrato.criadoPor && !compartilhamentos.some((c) => c.usuarioId === u.id)
                )}
                onConcluido={() => {
                  setMostrarCompartilhar(false)
                  contratoUsuariosApi.listarPorContrato(contratoId).then(setCompartilhamentos)
                }}
              />
            ) : (
              <button className="btn btn-secondary" onClick={() => setMostrarCompartilhar(true)}>
                + Compartilhar com alguém
              </button>
            )}
          </>
        )}

        <button className="btn btn-secondary" style={{ marginTop: 8 }} onClick={() => navigate('/arquivos')}>
          Voltar
        </button>
      </div>
    </div>
  )
}

function FormularioCompartilhar({
  contratoId,
  usuariosDisponiveis,
  onConcluido
}: {
  contratoId: number
  usuariosDisponiveis: Usuario[]
  onConcluido: () => void
}) {
  const [usuarioId, setUsuarioId] = useState<number | ''>('')
  const [podeEditar, setPodeEditar] = useState(false)
  const [podeAssinar, setPodeAssinar] = useState(false)
  const [podeExcluir, setPodeExcluir] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!usuarioId) return
    setEnviando(true)
    setErro(null)
    try {
      await contratoUsuariosApi.compartilhar({
        contratoId,
        usuarioId: Number(usuarioId),
        podeVisualizar: true,
        podeEditar,
        podeAssinar,
        podeExcluir
      })
      onConcluido()
    } catch {
      setErro('Não foi possível compartilhar o contrato.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ marginTop: 10 }}>
      {erro && <div className="status-message error">{erro}</div>}
      <div className="field">
        <label>Usuário</label>
        <select value={usuarioId} onChange={(e) => setUsuarioId(Number(e.target.value))} required>
          <option value="" disabled>
            Selecione alguém da empresa
          </option>
          {usuariosDisponiveis.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome} ({u.email})
            </option>
          ))}
        </select>
      </div>
      <div className="field" style={{ display: 'flex', flexDirection: 'row', gap: 14 }}>
        <label>
          <input type="checkbox" checked={podeEditar} onChange={(e) => setPodeEditar(e.target.checked)} /> Editar
        </label>
        <label>
          <input type="checkbox" checked={podeAssinar} onChange={(e) => setPodeAssinar(e.target.checked)} /> Assinar
        </label>
        <label>
          <input type="checkbox" checked={podeExcluir} onChange={(e) => setPodeExcluir(e.target.checked)} /> Excluir
        </label>
      </div>
      <button type="submit" className="btn btn-primary" disabled={enviando}>
        {enviando ? 'Compartilhando...' : 'Compartilhar'}
      </button>
    </form>
  )
}
