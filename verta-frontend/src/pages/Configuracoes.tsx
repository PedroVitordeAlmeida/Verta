import { useEffect, useState, type FormEvent } from 'react'
import { usuariosApi } from '../api/usuarios'
import { useAuth } from '../context/AuthContext'
import type { Usuario } from '../types'

const PERFIL_VAZIO = { nome: '', email: '', senha: '', perfil: 'COMUM', ativo: true }

export function Configuracoes() {
  const { usuario } = useAuth()
  const isAdmin = usuario?.perfil === 'ADMIN'

  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const [formAberto, setFormAberto] = useState(false)
  const [usuarioEmEdicao, setUsuarioEmEdicao] = useState<Usuario | null>(null)
  const [form, setForm] = useState(PERFIL_VAZIO)
  const [enviando, setEnviando] = useState(false)
  const [erroForm, setErroForm] = useState<string | null>(null)

  function carregarUsuarios() {
    if (!usuario) return
    setCarregando(true)
    usuariosApi
      .listar(usuario.empresaId)
      .then(setUsuarios)
      .catch(() => setErro('Não foi possível carregar os usuários.'))
      .finally(() => setCarregando(false))
  }

  useEffect(carregarUsuarios, [usuario])

  function abrirCriacao() {
    setUsuarioEmEdicao(null)
    setForm(PERFIL_VAZIO)
    setErroForm(null)
    setFormAberto(true)
  }

  function abrirEdicao(u: Usuario) {
    setUsuarioEmEdicao(u)
    setForm({ nome: u.nome, email: u.email, senha: '', perfil: u.perfil, ativo: u.ativo })
    setErroForm(null)
    setFormAberto(true)
  }

  function fecharForm() {
    setFormAberto(false)
    setUsuarioEmEdicao(null)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!usuario) return
    setEnviando(true)
    setErroForm(null)
    try {
      if (usuarioEmEdicao?.id) {
        await usuariosApi.atualizar(usuarioEmEdicao.id, {
          nome: form.nome,
          email: form.email,
          perfil: form.perfil,
          ativo: form.ativo,
          ...(form.senha ? { senha: form.senha } : {})
        })
      } else {
        await usuariosApi.criar({
          empresaId: usuario.empresaId,
          nome: form.nome,
          email: form.email,
          senha: form.senha,
          perfil: form.perfil,
          ativo: form.ativo
        })
      }
      fecharForm()
      carregarUsuarios()
    } catch {
      setErroForm('Não foi possível salvar o usuário. Confira os campos e tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  async function handleExcluir(u: Usuario) {
    if (!u.id) return
    if (!window.confirm(`Remover o usuário "${u.nome}"?`)) return
    try {
      await usuariosApi.remover(u.id)
      carregarUsuarios()
    } catch {
      setErro('Não foi possível remover o usuário.')
    }
  }

  return (
    <div className="card panel" style={{ maxWidth: 720 }}>
      <div className="panel-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        Usuários da empresa
        {isAdmin && !formAberto && (
          <button className="btn btn-primary" onClick={abrirCriacao}>
            + Novo usuário
          </button>
        )}
      </div>

      {erro && <div className="status-message error">{erro}</div>}

      {formAberto && (
        <form className="form-panel" onSubmit={handleSubmit} style={{ marginBottom: 16 }}>
          <div className="form-panel-title">
            {usuarioEmEdicao ? `Editar ${usuarioEmEdicao.nome}` : 'Novo usuário'}
          </div>

          {erroForm && <div className="status-message error">{erroForm}</div>}

          <div className="field">
            <label>Nome</label>
            <input
              value={form.nome}
              onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label>E-mail</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              required
            />
          </div>
          <div className="field">
            <label>{usuarioEmEdicao ? 'Nova senha (deixe em branco para manter)' : 'Senha'}</label>
            <input
              type="password"
              value={form.senha}
              onChange={(e) => setForm((f) => ({ ...f, senha: e.target.value }))}
              required={!usuarioEmEdicao}
            />
          </div>
          <div className="field">
            <label>Perfil</label>
            <select
              value={form.perfil}
              onChange={(e) => setForm((f) => ({ ...f, perfil: e.target.value }))}
            >
              <option value="ADMIN">Administrador</option>
              <option value="COMUM">Comum</option>
            </select>
          </div>
          <div className="field">
            <label>
              <input
                type="checkbox"
                checked={form.ativo}
                onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))}
                style={{ width: 'auto', marginRight: 8 }}
              />
              Usuário ativo
            </label>
          </div>

          <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={fecharForm}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={enviando}>
              {enviando ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      )}

      {carregando ? (
        <div className="loading-text">Carregando...</div>
      ) : (
        usuarios.map((u) => (
          <div className="recent-row" key={u.id}>
            <div>
              <div className="recent-row-name">{u.nome}</div>
              <div className="file-sub">{u.email}</div>
            </div>
            <div className="recent-row-meta" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <span className={`badge ${u.ativo ? 'badge-finalizado' : 'badge-arquivado'}`}>
                {u.perfil}
              </span>
              {isAdmin && (
                <>
                  <button className="btn btn-secondary" onClick={() => abrirEdicao(u)}>
                    Editar
                  </button>
                  <button className="btn btn-secondary" onClick={() => handleExcluir(u)}>
                    Excluir
                  </button>
                </>
              )}
            </div>
          </div>
        ))
      )}
    </div>
  )
}
