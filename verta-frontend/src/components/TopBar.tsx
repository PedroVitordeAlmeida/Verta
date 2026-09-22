import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { contratosApi } from '../api/contratos'
import { StatusBadge } from './StatusBadge'
import type { Contrato } from '../types'

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
  const navigate = useNavigate()
  const [termo, setTermo] = useState('')
  const [resultados, setResultados] = useState<Contrato[]>([])
  const [buscando, setBuscando] = useState(false)
  const [mostrarResultados, setMostrarResultados] = useState(false)
  const wrapRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function aoClicarFora(e: MouseEvent) {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) {
        setMostrarResultados(false)
      }
    }
    document.addEventListener('mousedown', aoClicarFora)
    return () => document.removeEventListener('mousedown', aoClicarFora)
  }, [])

  useEffect(() => {
    const termoBusca = termo.trim().toLowerCase()
    if (termoBusca.length < 2) {
      setResultados([])
      return
    }
    let ativo = true
    setBuscando(true)
    const timeout = setTimeout(() => {
      contratosApi
        .listar()
        .then((todos) => {
          if (!ativo) return
          setResultados(todos.filter((c) => c.titulo.toLowerCase().includes(termoBusca)).slice(0, 8))
        })
        .finally(() => ativo && setBuscando(false))
    }, 250)
    return () => {
      ativo = false
      clearTimeout(timeout)
    }
  }, [termo])

  function irParaContrato(contrato: Contrato) {
    setTermo('')
    setResultados([])
    setMostrarResultados(false)
    navigate(`/contratos/${contrato.id}`)
  }

  const termoValido = termo.trim().length >= 2

  return (
    <header className="topbar">
      <div className="topbar-title">{titulo}</div>
      <div className="topbar-right">
        <div className="topbar-search-wrap" ref={wrapRef}>
          <input
            className="topbar-search"
            placeholder="Buscar contratos..."
            value={termo}
            onChange={(e) => {
              setTermo(e.target.value)
              setMostrarResultados(true)
            }}
            onFocus={() => setMostrarResultados(true)}
          />
          {mostrarResultados && termoValido && (
            <div className="topbar-search-resultados">
              {buscando ? (
                <div className="topbar-search-item topbar-search-vazio">Buscando...</div>
              ) : resultados.length === 0 ? (
                <div className="topbar-search-item topbar-search-vazio">Nenhum contrato encontrado.</div>
              ) : (
                resultados.map((c) => (
                  <div className="topbar-search-item" key={c.id} onClick={() => irParaContrato(c)}>
                    <span className="topbar-search-item-titulo">{c.titulo}</span>
                    <StatusBadge status={c.status} />
                  </div>
                ))
              )}
            </div>
          )}
        </div>
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
