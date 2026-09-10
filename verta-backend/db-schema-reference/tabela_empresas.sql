-- public.empresas definição

-- Drop table

-- DROP TABLE public.empresas;

CREATE TABLE public.empresas (
	id serial4 NOT NULL,
	nome varchar(150) NOT NULL,
	cnpj varchar(18) NULL,
	data_cadastro timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT empresas_cnpj_key UNIQUE (cnpj),
	CONSTRAINT empresas_id_not_null NOT NULL id,
	CONSTRAINT empresas_nome_not_null NOT NULL nome,
	CONSTRAINT empresas_pkey PRIMARY KEY (id)
);