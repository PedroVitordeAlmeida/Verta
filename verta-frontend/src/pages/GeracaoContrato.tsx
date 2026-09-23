import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { templatesApi, extrairVariaveis } from '../api/templates'
import { contratosApi } from '../api/contratos'
import { useAuth } from '../context/AuthContext'
import { ConteudoRenderizado } from '../utils/formatarConteudo'
import type { Template } from '../types'

const NOMES_DE_GRUPO: Record<string, string> = {
  contratante: 'Contratante',
  contratado: 'Contratado',
  condicoes: 'Condições',
  valor: 'Condições',
  data: 'Condições'
}

function grupoDaVariavel(nomeVariavel: string): string {
  const prefixo = nomeVariavel.split('_')[0]
  if (NOMES_DE_GRUPO[prefixo]) return NOMES_DE_GRUPO[prefixo]
  return prefixo.charAt(0).toUpperCase() + prefixo.slice(1)
}

function rotuloDoCampo(nomeVariavel: string): string {
  const semPrefixo = nomeVariavel.includes('_')
    ? nomeVariavel.split('_').slice(1).join(' ')
    : nomeVariavel
  return semPrefixo.charAt(0).toUpperCase() + semPrefixo.slice(1).replace(/_/g, ' ')
}

export function GeracaoContrato() {
  const { usuario } = useAuth()
  const navigate = useNavigate()
  const [templates, setTemplates] = useState<Template[]>([])
  const [templateSelecionado, setTemplateSelecionado] = useState<Template | null>(null)
  const [titulo, setTitulo] = useState('')
  const [valores, setValores] = useState<Record<string, string>>({})
  const [carregando, setCarregando] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  useEffect(() => {
    if (!usuario) return
    let ativo = true
    templatesApi
      .listar(usuario.empresaId)
      .then((resp) => ativo && setTemplates(resp.filter((t) => t.ativo)))
      .catch(() => ativo && setErro('Não foi possível carregar os templates.'))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [usuario])

  const variaveis = useMemo(
    () => (templateSelecionado ? extrairVariaveis(templateSelecionado.conteudo) : []),
    [templateSelecionado]
  )

  const grupos = useMemo(() => {
    const mapa = new Map<string, string[]>()
    for (const v of variaveis) {
      const grupo = grupoDaVariavel(v)
      if (!mapa.has(grupo)) mapa.set(grupo, [])
      mapa.get(grupo)!.push(v)
    }
    return Array.from(mapa.entries())
  }, [variaveis])

  function selecionarTemplate(template: Template) {
    setTemplateSelecionado(template)
    setValores({})
    setTitulo(template.nome)
    setSucesso(null)
    setErro(null)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!templateSelecionado || !usuario || !templateSelecionado.id) return
    setEnviando(true)
    setErro(null)
    setSucesso(null)
    try {
      const contrato = await contratosApi.gerarDeTemplate({
        empresaId: usuario.empresaId,
        templateId: templateSelecionado.id,
        titulo,
        criadoPor: usuario.usuarioId,
        valores
      })
      setSucesso(`Contrato "${contrato.titulo}" gerado com sucesso.`)
    } catch {
      setErro('Não foi possível gerar o contrato. Confira os campos e tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  if (carregando) {
    return <div className="loading-text">Carregando templates...</div>
  }

  if (!templateSelecionado) {
    return (
      <div className="card form-panel" style={{ maxWidth: 640 }}>
        <div className="form-panel-title">Escolha um template</div>
        <div className="form-panel-hint">Selecione o modelo que vai servir de base para o novo contrato.</div>
        {templates.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-title">Nenhum template ativo</div>
            Cadastre um template em "Gerenciador de Arquivos" antes de gerar um contrato.
          </div>
        ) : (
          <div className="template-picker">
            {templates.map((t) => (
              <button key={t.id} className="template-option" onClick={() => selecionarTemplate(t)}>
                <span className="template-option-name">{t.nome}</span>
                {t.descricao && <span className="template-option-desc">{t.descricao}</span>}
              </button>
            ))}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="contract-layout">
      <div className="card">
        <div className="editor-body">
          <ConteudoRenderizado conteudo={templateSelecionado.conteudo} valores={valores} />
        </div>
      </div>

      <form className="card form-panel" onSubmit={handleSubmit}>
        <div className="form-panel-title">Campos do contrato</div>
        <div className="form-panel-hint">
          Preencha as variáveis do template. Elas aparecem destacadas no documento ao lado.
        </div>

        {erro && <div className="status-message error">{erro}</div>}
        {sucesso && <div className="status-message success">{sucesso}</div>}

        <div className="field">
          <label>Título do contrato</label>
          <input value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
        </div>

        {grupos.map(([grupo, campos]) => (
          <div key={grupo}>
            <div className="section-title">{grupo.toUpperCase()}</div>
            {campos.map((nomeVar) => (
              <div className="field" key={nomeVar}>
                <div className="field-label-row">
                  <label>{rotuloDoCampo(nomeVar)}</label>
                  <span className="field-tag">{`{{${nomeVar}}}`}</span>
                </div>
                <input
                  value={valores[nomeVar] ?? ''}
                  onChange={(e) => setValores((v) => ({ ...v, [nomeVar]: e.target.value }))}
                />
              </div>
            ))}
          </div>
        ))}

        <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => setTemplateSelecionado(null)}
          >
            Trocar template
          </button>
          <button type="submit" className="btn btn-primary" disabled={enviando}>
            {enviando ? 'Gerando...' : 'Gerar contrato'}
          </button>
        </div>

        {sucesso && (
          <button
            type="button"
            className="btn btn-secondary"
            style={{ marginTop: 10 }}
            onClick={() => navigate('/arquivos')}
          >
            Ver em Gerenciador de Arquivos
          </button>
        )}
      </form>
    </div>
  )
}
