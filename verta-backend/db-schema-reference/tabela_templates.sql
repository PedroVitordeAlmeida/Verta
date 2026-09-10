-- public.templates definição

-- Drop table

-- DROP TABLE public.templates;

CREATE TABLE public.templates (
	id serial4 NOT NULL,
	empresa_id int4 NOT NULL,
	nome varchar(150) NOT NULL,
	descricao text NULL,
	conteudo text NOT NULL,
	ativo bool DEFAULT true NULL,
	data_criacao timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT templates_conteudo_not_null NOT NULL conteudo,
	CONSTRAINT templates_empresa_id_not_null NOT NULL empresa_id,
	CONSTRAINT templates_id_not_null NOT NULL id,
	CONSTRAINT templates_nome_not_null NOT NULL nome,
	CONSTRAINT templates_pkey PRIMARY KEY (id)
);


-- public.templates chaves estrangeiras

ALTER TABLE public.templates ADD CONSTRAINT fk_template_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);