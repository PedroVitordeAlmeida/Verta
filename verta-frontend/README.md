# Verta - Frontend

Frontend do **Verta**, feito em **React + TypeScript + Vite**, replicando as
telas desenhadas no Figma (Dashboard, Gerenciador de Arquivos, Geração de
Contrato, Assinatura Digital, Planos e Cobrança, Configurações) e já
consumindo a API do `verta-backend`.

Este pacote tambem foi preparado para ser **exportado e entregue a quem for
montar o `docker-compose`**, do mesmo jeito que o backend.

## 1. Estrutura

```
verta-frontend/
├── Dockerfile          # build multi-stage (node -> nginx), pronto pra usar
├── nginx.conf           # serve os arquivos estaticos + rotas do React
├── .env.example          # variavel VITE_API_URL
├── package.json
└── src/
    ├── main.tsx / App.tsx    # rotas (react-router-dom)
    ├── api/                  # um arquivo por recurso, chamando o backend (axios)
    ├── context/AuthContext.tsx  # guarda o usuario logado + token JWT
    ├── components/            # Sidebar, TopBar, StatCard, StatusBadge, Layout
    ├── pages/                 # uma pagina por tela do Figma
    ├── types/                 # espelham os DTOs do backend
    └── styles/global.css      # cores, tipografia e layout (tema navy/azul do Figma)
```

## 2. Importante: `VITE_API_URL` é usada em tempo de BUILD

Diferente de uma variável de ambiente comum, o Vite grava o valor de
`VITE_API_URL` dentro dos arquivos `.js` finais **no momento do build**, não
quando o container roda. Por isso, no `Dockerfile`, ela é recebida como
`ARG` (não apenas `ENV`), e quem for montar o `docker-compose` precisa
passá-la em `build.args`, apontando para onde o backend vai estar acessível
(ex: a URL pública da API, ou o nome do serviço se o front tiver algum
proxy — o mais comum é usar a URL publica mesmo, já que quem faz as
chamadas é o navegador da pessoa usando o site, não o container).

```yaml
frontend:
  build:
    context: ./verta-frontend
    args:
      VITE_API_URL: https://api.verta.exemplo.com
```

## 3. Docker

```bash
docker build -t verta-frontend --build-arg VITE_API_URL=http://localhost:8080 .
docker run -p 3000:80 verta-frontend
```

## 4. Rodando localmente (sem Docker)

```bash
npm install
cp .env.example .env   # ajuste VITE_API_URL se o backend nao estiver em localhost:8080
npm run dev
```

Abre em `http://localhost:5173`.

## 5. Sobre o login

O backend exige e-mail + senha (tabela `usuarios`, com senha em hash
bcrypt). **Atenção**: no backup `VERTA_backup.sql` que veio de exemplo, a
coluna `senha` dos usuários de teste está com o valor `123456` **em texto
puro**, não em hash bcrypt. Como o backend só aceita senha em hash, o login
com esses usuários de exemplo vai falhar até que:

- alguém gere o hash bcrypt de `123456` e atualize a coluna `senha` desses
  usuários no banco, **ou**
- seja criado um novo usuário de teste pela rota `POST /usuarios` (que já
  gera o hash certinho na hora do cadastro).

## 6. Telas implementadas

| Tela do Figma | Rota | O que faz |
|---|---|---|
| Dashboard | `/` | Cards de estatística + contratos recentes + ações rápidas |
| Gerenciador de Arquivos | `/arquivos` | Pastas (Meus Contratos, Templates, Contratos Gerados/Assinados/Arquivados) + registro manual de arquivo |
| Geração de Contrato | `/contratos/gerar` | Escolhe um template, preenche as variáveis `{{campo}}` com preview ao vivo, gera o contrato |
| Assinatura Digital | `/assinatura` | Tela de espera - ainda não há integração com um provedor de assinatura |
| Planos e Cobrança | `/planos` | Tela de espera - ainda não existe tabela de planos no banco |
| Configurações | `/configuracoes` | Lista os usuários cadastrados da empresa logada |

## 7. Próximos passos

- Ligar a Assinatura Digital a um provedor real (Clicksign, DocuSign, etc).
- Criar a tabela e as rotas de Planos e Cobrança no backend antes de dar
  vida a essa tela.
- Upload de arquivo de verdade (hoje o formulário em "Gerenciador de
  Arquivos" só registra o *caminho* do arquivo, não faz upload do binário).
