interface TopBarProps {
  titulo: string
  nomeEmpresa: string
  perfil: string
}

function iniciais(nome: string): string {
  return nome
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0]?.toUpperCase())
    .join('')
}

export function TopBar({ titulo, nomeEmpresa, perfil }: TopBarProps) {
  return (
    <header className="topbar">
      <div className="topbar-title">{titulo}</div>
      <div className="topbar-right">
        <input className="topbar-search" placeholder="Buscar contratos..." />
        <div className="topbar-user">
          <div className="topbar-avatar">{iniciais(nomeEmpresa)}</div>
          <div>
            <div className="topbar-user-name">{nomeEmpresa}</div>
            <div className="topbar-user-role">{perfil}</div>
          </div>
        </div>
      </div>
    </header>
  )
}
