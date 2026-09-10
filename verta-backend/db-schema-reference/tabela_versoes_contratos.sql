-- public.versoes_contrato definição

-- Drop table

-- DROP TABLE public.versoes_contrato;

CREATE TABLE public.versoes_contrato (
	id serial4 NOT NULL,
	contrato_id int4 NOT NULL,
	numero_versao int4 NOT NULL,
	conteudo text NOT NULL,
	criado_por int4 NOT NULL,
	data_criacao timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT uk_contrato_versao UNIQUE (contrato_id, numero_versao),
	CONSTRAINT versoes_contrato_conteudo_not_null NOT NULL conteudo,
	CONSTRAINT versoes_contrato_contrato_id_not_null NOT NULL contrato_id,
	CONSTRAINT versoes_contrato_criado_por_not_null NOT NULL criado_por,
	CONSTRAINT versoes_contrato_id_not_null NOT NULL id,
	CONSTRAINT versoes_contrato_numero_versao_not_null NOT NULL numero_versao,
	CONSTRAINT versoes_contrato_pkey PRIMARY KEY (id)
);


-- public.versoes_contrato chaves estrangeiras

ALTER TABLE public.versoes_contrato ADD CONSTRAINT fk_versao_contrato FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);
ALTER TABLE public.versoes_contrato ADD CONSTRAINT fk_versao_usuario FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);