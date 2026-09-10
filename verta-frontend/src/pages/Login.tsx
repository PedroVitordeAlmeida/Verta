import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Login() {
  const { usuario, login, carregando } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  if (usuario) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    try {
      await login(email, senha)
      navigate('/')
    } catch {
      setErro('E-mail ou senha inválidos.')
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-logo">Verta</div>
        <div className="login-subtitle">Entre para gerenciar seus contratos e templates.</div>

        {erro && <div className="login-error">{erro}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="voce@empresa.com"
              required
              autoFocus
            />
          </div>
          <div className="field">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary login-submit" disabled={carregando}>
            {carregando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <div className="login-hint">
          Acesso restrito aos usuários cadastrados da sua empresa no Verta.
        </div>
      </div>
    </div>
  )
}
