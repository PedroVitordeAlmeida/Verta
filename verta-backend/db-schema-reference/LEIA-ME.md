# Sobre estes arquivos

Os arquivos `tabela_*.sql` sao o schema **original** que voce me passou no
inicio do projeto (do zip `tabela_arquivos.zip`).

O arquivo `schema_atual_VERTA_backup.sql` foi extraido do backup real do
banco (`VERTA_backup.sql`) que te enviaram depois. **Esse e o schema
atualmente correto** - o backend ja foi ajustado para bater com ele.

Principais diferencas do schema original pro atual:

- Nova tabela `contrato_usuarios`: controla quem pode visualizar, editar,
  assinar ou excluir cada contrato.
- A tabela `arquivos` ganhou a coluna `versao_id` (obrigatoria): agora cada
  arquivo esta ligado a uma versao especifica do contrato, nao so ao
  contrato em geral.
- `contratos.status` agora tem uma regra fixa (CHECK) permitindo somente:
  `RASCUNHO`, `EM_REVISAO`, `FINALIZADO`, `ARQUIVADO`, `CANCELADO`.
- Foram adicionadas restricoes extras (`empresa_id` combinado com o id em
  contratos/templates/usuarios) pra garantir que um contrato nunca fique
  ligado a um template ou usuario de outra empresa por engano.

O arquivo `migration_planos_cobranca.sql` e uma migration separada (rodar depois
do schema acima ja estar aplicado): cria a tabela `planos`, os campos de
assinatura em `empresas` (plano/periodo/vigencia) e as flags de
`validado_juridicamente`/`origem_sistema` em `templates`, usadas pelo recurso
de Planos e Cobranca.
