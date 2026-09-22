-- Migracao: planos e limites por empresa (Basico/Plus).
--
-- Rode cada bloco na ordem abaixo, direto no Postgres onde o VERTA roda (psql ou
-- outro client). O backend NAO roda migrations sozinho - ele so espera que essas
-- tabelas/colunas ja existam.

-- 1) Novo valor de enum: SUPERADMIN (equipe Verta/Dalcomad - unico perfil que pode
--    atribuir plano a uma empresa cliente). Precisa ser commitado antes de qualquer
--    INSERT/UPDATE que use o valor novo, entao rode esse comando sozinho primeiro.
ALTER TYPE public.tipo_perfil ADD VALUE IF NOT EXISTS 'SUPERADMIN';

-- 2) Catalogo fixo de planos.
CREATE TABLE public.planos (
	id serial4 NOT NULL,
	nome varchar(30) NOT NULL,
	max_usuarios int4 NOT NULL,
	max_templates int4 NOT NULL,
	validacao_juridica_base bool NOT NULL DEFAULT false,
	quota_geracao_juridica_mensal int4 NULL,
	CONSTRAINT planos_pkey PRIMARY KEY (id),
	CONSTRAINT planos_nome_key UNIQUE (nome)
);

INSERT INTO public.planos (nome, max_usuarios, max_templates, validacao_juridica_base, quota_geracao_juridica_mensal) VALUES
	('BASICO', 10, 30, false, NULL),
	('PLUS', 20, 50, true, 5);

-- 3) Assinatura (plano contratado) de cada empresa. plano_id NULL = empresa sem plano
--    atribuido ainda -> nenhum limite e aplicado (comportamento anterior, sem quebrar
--    empresas ja cadastradas).
ALTER TABLE public.empresas ADD COLUMN plano_id int4 NULL REFERENCES public.planos(id);
ALTER TABLE public.empresas ADD COLUMN periodo_plano varchar(20) NULL;
ALTER TABLE public.empresas ADD COLUMN data_inicio_plano timestamp NULL;
ALTER TABLE public.empresas ADD COLUMN data_fim_plano timestamp NULL;
ALTER TABLE public.empresas ADD CONSTRAINT empresas_periodo_plano_check
	CHECK (periodo_plano IS NULL OR periodo_plano IN ('SEIS_MESES', 'UM_ANO', 'TRES_ANOS'));

-- 4) Marcacoes em templates: validado_juridicamente (true nos templates base do Plano
--    Plus) e origem_sistema (true nos 5 templates base clonados ao atribuir um plano -
--    nao contam na cota de "templates criados pela empresa").
ALTER TABLE public.templates ADD COLUMN validado_juridicamente bool NOT NULL DEFAULT false;
ALTER TABLE public.templates ADD COLUMN origem_sistema bool NOT NULL DEFAULT false;

-- 5) Depois de rodar o acima, promova o(s) usuario(s) da Dalcomad que vao gerenciar
--    planos das empresas clientes (troque o e-mail):
-- UPDATE public.usuarios SET perfil = 'SUPERADMIN' WHERE email = 'seu-email@dalcomad.com.br';
