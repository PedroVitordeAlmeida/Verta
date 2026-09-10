-- public.usuarios definição

-- Drop table

-- DROP TABLE public.usuarios;

CREATE TABLE public.usuarios (
	id serial4 NOT NULL,
	empresa_id int4 NOT NULL,
	nome varchar(150) NOT NULL,
	email varchar(150) NOT NULL,
	senha varchar(255) NOT NULL,
	perfil varchar(50) NOT NULL,
	ativo bool DEFAULT true NULL,
	data_cadastro timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT usuarios_email_key UNIQUE (email),
	CONSTRAINT usuarios_email_not_null NOT NULL email,
	CONSTRAINT usuarios_empresa_id_not_null NOT NULL empresa_id,
	CONSTRAINT usuarios_id_not_null NOT NULL id,
	CONSTRAINT usuarios_nome_not_null NOT NULL nome,
	CONSTRAINT usuarios_perfil_not_null NOT NULL perfil,
	CONSTRAINT usuarios_pkey PRIMARY KEY (id),
	CONSTRAINT usuarios_senha_not_null NOT NULL senha
);


-- public.usuarios chaves estrangeiras

ALTER TABLE public.usuarios ADD CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);