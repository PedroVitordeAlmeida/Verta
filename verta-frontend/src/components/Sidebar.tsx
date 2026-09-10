import { NavLink } from 'react-router-dom'

const ITENS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/arquivos', label: 'Gerenciador de Arquivos' },
  { to: '/contratos/gerar', label: 'Geração de Contrato' },
  { to: '/assinatura', label: 'Assinatura Digital' },
  { to: '/planos', label: 'Planos e Cobrança' },
  { to: '/configuracoes', label: 'Configurações' }
]

export function Sidebar({ onLogout, nomeUsuario }: { onLogout: () => void; nomeUsuario: string }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">Verta</div>
      <nav className="sidebar-nav">
        {ITENS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
          >
            <span className="sidebar-dot" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div style={{ fontSize: 13, opacity: 0.85 }}>{nomeUsuario}</div>
        <button className="sidebar-logout" onClick={onLogout}>
          Sair
        </button>
      </div>
    </aside>
  )
}
