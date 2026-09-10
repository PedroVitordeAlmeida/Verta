# Verta - Backend

Backend do **Verta**, sistema de gerenciamento de templates e contratos para
empresas clientes. Escrito em **Kotlin** com **Ktor** (servidor HTTP) e
**Exposed** (acesso ao PostgreSQL ja existente).

Este pacote foi preparado para ser **exportado e entregue a quem for montar
o `docker-compose`** (backend + banco + front). Todas as configuracoes
sensiveis (host do banco, senha, segredo do JWT, etc.) sao lidas de
**variaveis de ambiente**, nunca hardcoded.

---

## 1. Estrutura do projeto

```
verta-backend/
├── Dockerfile                  # build multi-stage (gradle -> jre), pronto pra usar
├── docker-compose.example.yml  # MODELO de referencia (nao e usado automaticamente)
├── .env.example                # variaveis de ambiente esperadas
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── kotlin/com/verta/backend/
    │   ├── Application.kt          # ponto de entrada (EngineMain + module)
    │   ├── config/
    │   │   ├── DatabaseFactory.kt  # pool HikariCP + conexao Exposed
    │   │   └── JwtConfig.kt        # geracao/validacao de token JWT
    │   ├── models/                 # tabelas Exposed (espelham o banco ja criado)
    │   ├── dto/                    # objetos de entrada/saida da API (JSON)
    │   ├── repositories/           # regras de acesso a dados (CRUD por tabela)
    │   ├── routes/                 # endpoints HTTP por recurso
    │   ├── plugins/                # configuracao do Ktor (serializacao, CORS, auth, erros, rotas)
    │   └── security/PasswordUtil.kt # hash/verificacao de senha (bcrypt)
    └── resources/
        ├── application.conf        # le as env vars (HOCON)
        └── logback.xml
```

## 2. Banco de dados

O backend **nao cria as tabelas** — ele assume que o banco Postgres ja
existe. As classes em `models/` mapeiam o schema **atualizado**, que veio
no backup `VERTA_backup.sql`: `empresas`, `usuarios`, `templates`,
`contratos`, `versoes_contrato`, `arquivos` e `contrato_usuarios`.
Veja `db-schema-reference/LEIA-ME.md` para o historico do que mudou desde
a primeira versao do schema.

Se o ambiente onde o Docker for montado ainda nao tiver o banco criado, os
mesmos `.sql` podem ser colocados numa pasta `init-db/` e montados no
container do Postgres em `/docker-entrypoint-initdb.d` (ha um exemplo
comentado em `docker-compose.example.yml`).

## 3. Variaveis de ambiente (`.env`)

Veja `.env.example`. Resumo:

| Variavel | Para que serve |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexao com o Postgres |
| `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_REALM` | Autenticacao dos usuarios |
| `PORT` | Porta HTTP do backend dentro do container (padrao 8080) |
| `CORS_ALLOWED_HOST` | Host do front que vai consumir a API (ex: `frontend:3000`) |

**Quem for montar o docker-compose so precisa criar um `.env` (ou colocar as
envs direto no compose) com esses valores.** Nada mais precisa ser tocado
no codigo.

## 4. Docker

Ja existe um `Dockerfile` pronto (multi-stage: builda com Gradle, roda com
um JRE enxuto). Para gerar a imagem:

```bash
docker build -t verta-backend .
```

Para rodar isoladamente (apontando pra um Postgres acessivel):

```bash
docker run -p 8080:8080 \
  -e DB_HOST=meu_host_postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=verta \
  -e DB_USER=verta_user \
  -e DB_PASSWORD=minha_senha \
  -e JWT_SECRET=um_segredo_grande \
  verta-backend
```

O arquivo `docker-compose.example.yml` mostra como ligar `postgres` +
`backend` e `frontend` (que agora tambem ja existe, em `verta-frontend/`)
num compose real —
e so um modelo, quem for montar o ambiente final adapta os nomes de
servico/rede conforme necessario.

## 5. Rodando localmente pelo IntelliJ IDEA (sem Docker)

O projeto ja vem com o **Gradle Wrapper** (pasta `gradle/`, `gradlew` e
`gradlew.bat`), fixando a versao do Gradle em **8.7** - a mesma usada no
`Dockerfile`. Isso evita o erro `Could not get unknown property 'convention'`,
que acontece quando o Gradle baixa sozinho uma versao muito nova (9.x) e ela
nao e compativel com o plugin do Ktor. Com o wrapper presente, o IntelliJ
detecta e usa a versao certa automaticamente - nao precisa configurar nada.

1. Abrir a pasta `verta-backend` no IntelliJ (ele detecta o Gradle
   automaticamente pelo `build.gradle.kts`).
2. Deixar o IntelliJ baixar as dependencias (precisa de internet na
   primeira vez, para o Maven Central).
3. Configurar as variaveis de ambiente na configuracao de Run
   (`Application.kt` > Edit Configurations > Environment variables),
   usando os mesmos nomes do `.env.example`, apontando `DB_HOST` para o
   Postgres local (ex: `localhost`).
4. Rodar `Application.kt` (funcao `main`).

## 5.1 Sobre o login e o `VERTA_backup.sql`

Se o banco for restaurado a partir do `VERTA_backup.sql` (dados de teste),
repare que a coluna `senha` dos 4 usuarios de exemplo esta gravada como
`123456` em **texto puro**. O backend so aceita senha em hash bcrypt (e' o
que a rota `POST /usuarios` gera automaticamente), entao o login com esses
usuarios de teste falha ate rodar este comando no banco (troca a senha
desses 4 usuarios pela versao criptografada de `123456`):

```sql
UPDATE usuarios SET senha = '$2b$12$hkzY.TR4B2NKCR6R5YnDgeq8Y4kUFHNeJyaryDHyPtwMbvdIG7NtK';
```

Depois disso, qualquer um destes logins funciona com a senha `123456`:
`carlos@verticetecnologia.com`, `mariana@verticetecnologia.com`,
`ana@alfaconsultoria.com`, `joao@verticetecnologia.com`.

Isso e' util so pra testes - em producao, o certo e cadastrar usuarios
reais pela rota `POST /usuarios`, que ja grava a senha em hash certinho.

## 6. Endpoints principais

Todos (exceto `/health` e `/auth/login`) exigem o header
`Authorization: Bearer <token>` obtido no login.

| Metodo | Rota | Descricao |
|---|---|---|
| POST | `/auth/login` | Login (email + senha) → retorna token JWT |
| GET | `/health` | Healthcheck (usado pelo Docker) |
| GET/POST/PUT/DELETE | `/empresas`, `/empresas/{id}` | CRUD de empresas clientes |
| GET/POST/PUT/DELETE | `/usuarios`, `/usuarios/{id}` | CRUD de usuarios |
| GET/POST/PUT/DELETE | `/templates`, `/templates/{id}` | CRUD de templates de contrato |
| GET/POST/DELETE | `/contratos`, `/contratos/{id}` | CRUD de contratos |
| PATCH | `/contratos/{id}/status` | Atualiza status. Valores aceitos: `RASCUNHO`, `EM_REVISAO`, `FINALIZADO`, `ARQUIVADO`, `CANCELADO` |
| POST | `/contratos/gerar` | Tela "Geracao de Contrato": recebe `templateId` + valores das variaveis `{{campo}}` e ja cria o contrato + 1ª versao |
| GET/POST | `/versoes-contrato?contratoId=` | Historico de versoes de um contrato |
| GET/POST/PUT/DELETE | `/contrato-usuarios?contratoId=` ou `?usuarioId=` | Quem pode ver/editar/assinar/excluir cada contrato |
| GET/POST/DELETE | `/arquivos?contratoId=` ou `?versaoId=` | Metadados de arquivos anexados a uma versao de contrato |
| GET | `/dashboard/stats?empresaId=` | Numeros dos 4 cards da tela Dashboard |

## 7. O que ainda fica em aberto (proximos passos)

- **Upload fisico de arquivo**: hoje `/arquivos` so grava metadados
  (nome, caminho, tipo, tamanho) na tabela `arquivos`. O upload do binario
  em si (disco, volume Docker ou S3/Blob) ainda precisa ser plugado —
  o campo `caminhoArquivo` ja esta pronto para receber esse caminho final.
- **Assinatura digital**: ainda nao ha integracao com um provedor de
  assinatura (ex: Clicksign, DocuSign); a tela "Assinatura Digital" do
  Figma precisara de um servico proprio ou de terceiros.
- **Planos e Cobranca**: nao ha tabela nem endpoint para isso ainda —
  nao existia no banco que voce enviou.
- **Perfis/permissoes**: o campo `perfil` do usuario ja viaja no token
  JWT, mas as rotas ainda nao bloqueiam acoes por perfil (ex: so admin
  pode excluir empresa). Pode ser adicionado depois nos `routes/`.
