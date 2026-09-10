import { useEffect, useState } from 'react'
import { usuariosApi } from '../api/usuarios'
import { useAuth } from '../context/AuthContext'
import type { Usuario } from '../types'

export function Configuracoes() {
  const { usuario } = useAuth()
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (!usuario) return
    let ativo = true
    usuariosApi
      .listar(usuario.empresaId)
      .then((resp) => ativo && setUsuarios(resp))
      .catch(() => ativo && setErro('Não foi possível carregar os usuários.'))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [usuario])

  return (
    <div className="card panel" style={{ maxWidth: 720 }}>
      <div className="panel-title">Usuários da empresa</div>
      {erro && <div className="status-message error">{erro}</div>}
      {carregando ? (
        <div className="loading-text">Carregando...</div>
      ) : (
        usuarios.map((u) => (
          <div className="recent-row" key={u.id}>
            <div>
              <div className="recent-row-name">{u.nome}</div>
              <div className="file-sub">{u.email}</div>
            </div>
            <div className="recent-row-meta">
              <span className={`badge ${u.ativo ? 'badge-finalizado' : 'badge-arquivado'}`}>
                {u.perfil}
              </span>
            </div>
          </div>
        ))
      )}
    </div>
  )
}
