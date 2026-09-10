-- public.contratos definição

-- Drop table

-- DROP TABLE public.contratos;

CREATE TABLE public.contratos (
	id serial4 NOT NULL,
	empresa_id int4 NOT NULL,
	template_id int4 NULL,
	titulo varchar(200) NOT NULL,
	tipo varchar(100) NULL,
	status varchar(50) DEFAULT 'RASCUNHO'::character varying NOT NULL,
	criado_por int4 NOT NULL,
	data_criacao timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	data_atualizacao timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT contratos_criado_por_not_null NOT NULL criado_por,
	CONSTRAINT contratos_empresa_id_not_null NOT NULL empresa_id,
	CONSTRAINT contratos_id_not_null NOT NULL id,
	CONSTRAINT contratos_pkey PRIMARY KEY (id),
	CONSTRAINT contratos_status_not_null NOT NULL status,
	CONSTRAINT contratos_titulo_not_null NOT NULL titulo
);


-- public.contratos chaves estrangeiras

ALTER TABLE public.contratos ADD CONSTRAINT fk_contrato_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);
ALTER TABLE public.contratos ADD CONSTRAINT fk_contrato_template FOREIGN KEY (template_id) REFERENCES public.templates(id);
ALTER TABLE public.contratos ADD CONSTRAINT fk_contrato_usuario FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);