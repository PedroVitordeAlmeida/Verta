import { useState } from 'react'

type PlanoId = 'BASICO' | 'PLUS'

interface PlanoInfo {
  id: PlanoId
  nome: string
  descricao: string
  destaque?: boolean
  recursos: string[]
  periodos: string[]
}

const PLANOS: PlanoInfo[] = [
  {
    id: 'BASICO',
    nome: 'Básico',
    descricao: 'Para empresas que estão começando a organizar seus contratos.',
    periodos: ['6 meses', '1 ano', '3 anos'],
    recursos: [
      '5 templates base disponibilizados por nós, sem validação jurídica',
      '10 usuários disponibilizados por empresa',
      'Criação de até 30 templates de contratos pela empresa'
    ]
  },
  {
    id: 'PLUS',
    nome: 'Plus',
    descricao: 'Para empresas que querem contratos com respaldo jurídico próprio.',
    destaque: true,
    periodos: ['6 meses', '1 ano', '3 anos'],
    recursos: [
      '5 templates base disponibilizados por nós, com validação jurídica',
      '20 usuários disponibilizados por empresa',
      'Criação de até 50 templates de contratos pela empresa',
      'Equipe jurídica própria disponível para validar até 5 templates por mês'
    ]
  }
]

/**
 * Tela puramente visual por enquanto (nao ha integracao com o backend/banco): so mostra
 * os 2 planos e deixa o Plus marcado como ativo, ja que a empresa conta com juridico proprio.
 */
export function PlanosCobranca() {
  const [planoAtivo, setPlanoAtivo] = useState<PlanoId>('PLUS')

  return (
    <div>
      <div className="panel-title" style={{ marginBottom: 4 }}>
        Planos e Cobrança
      </div>
      <div className="file-sub" style={{ marginBottom: 20 }}>
        Escolha o plano ideal para a sua empresa.
      </div>

      <div className="planos-grid">
        {PLANOS.map((plano) => (
          <PlanoCard
            key={plano.id}
            plano={plano}
            ativo={planoAtivo === plano.id}
            onSelecionar={() => setPlanoAtivo(plano.id)}
          />
        ))}
      </div>
    </div>
  )
}

function PlanoCard({
  plano,
  ativo,
  onSelecionar
}: {
  plano: PlanoInfo
  ativo: boolean
  onSelecionar: () => void
}) {
  return (
    <div
      className={`card plano-card${ativo ? ' selecionado' : ''}${plano.destaque && !ativo ? ' destaque' : ''}`}
    >
      {plano.destaque && <div className="plano-card-tag">Recomendado</div>}

      <div className="plano-card-header">
        <div className="plano-card-nome">Plano {plano.nome}</div>
        {ativo && <span className="badge badge-finalizado">Plano ativo</span>}
      </div>
      <div className="file-sub">{plano.descricao}</div>

      <ul className="plano-card-recursos">
        {plano.recursos.map((recurso) => (
          <li key={recurso}>
            <span className="plano-check">✓</span>
            {recurso}
          </li>
        ))}
      </ul>

      <div className="plano-card-periodos">
        <div className="plano-card-periodos-label">Disponível em:</div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {plano.periodos.map((periodo) => (
            <span key={periodo} className="badge badge-arquivado">
              {periodo}
            </span>
          ))}
        </div>
      </div>

      <button
        type="button"
        className={ativo ? 'btn btn-primary' : 'btn btn-secondary'}
        onClick={onSelecionar}
        disabled={ativo}
        style={{ marginTop: 20, justifyContent: 'center' }}
      >
        {ativo ? '✓ Plano ativo' : 'Selecionar plano'}
      </button>
    </div>
  )
}
