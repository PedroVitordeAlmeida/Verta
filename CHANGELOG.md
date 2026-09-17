# Changelog

Registro das modificações feitas no sistema Verta, por semana, para consulta futura.

## Semana de 2026-09-16

### Sessão única por usuário
- Cada login agora gera um identificador de sessão novo, salvo em `usuarios.token_sessao`, e sobrescreve o anterior.
- A validação do JWT (a cada requisição) confere se esse identificador ainda bate com o salvo no banco; se não bater (login feito em outro lugar depois), a sessão antiga é invalidada automaticamente (401 → o frontend redireciona pro login sozinho).
- Adicionado `POST /auth/logout`, que limpa esse identificador no banco.
- **Banco:** foi preciso adicionar a coluna `usuarios.token_sessao varchar(255) NULL`.

### `perfil` como ENUM nativo do Postgres
- A coluna `usuarios.perfil` passou a ser o tipo `tipo_perfil` (`ADMIN` / `COMUM`) em vez de texto livre.
- O backend (Kotlin/Exposed) foi ajustado para ler/gravar esse enum nativo corretamente.
- **Banco:** mudança de schema feita diretamente por vocês (`CREATE TYPE tipo_perfil` + `ALTER TABLE usuarios ALTER COLUMN perfil TYPE tipo_perfil`).

### Gerenciamento de usuários restrito a ADMIN
- Criar, editar e excluir usuário (`POST` / `PUT` / `DELETE /usuarios`) agora exige perfil `ADMIN` (retorna 403 para usuário `COMUM`).
- Um admin só consegue gerenciar usuários da própria empresa, mesmo tentando forçar outro `empresaId`.
- Listar/ver usuário continua liberado a qualquer usuário logado (para ver a própria equipe).
- **Frontend:** a tela "Configurações" ganhou formulário de criar/editar/excluir usuário (nome, e-mail, senha, perfil, ativo), visível somente quando logado como `ADMIN`.

### Observações
- Nenhuma dessas mudanças usa arquivo de migração automática (o projeto não tem Flyway/Liquibase) — toda alteração de schema é aplicada manualmente no banco.
- Os arquivos de referência de schema em `verta-backend/db-schema-reference/` (`tabela_usuarios.sql`, `schema_atual_VERTA_backup.sql`) foram atualizados para refletir essas duas colunas novas.
