-- public.arquivos definição

-- Drop table

-- DROP TABLE public.arquivos;

CREATE TABLE public.arquivos (
	id serial4 NOT NULL,
	contrato_id int4 NOT NULL,
	nome_arquivo varchar(255) NOT NULL,
	caminho_arquivo varchar(500) NOT NULL,
	tipo_arquivo varchar(100) NULL,
	tamanho int8 NULL,
	data_armazenamento timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT arquivos_caminho_arquivo_not_null NOT NULL caminho_arquivo,
	CONSTRAINT arquivos_contrato_id_not_null NOT NULL contrato_id,
	CONSTRAINT arquivos_id_not_null NOT NULL id,
	CONSTRAINT arquivos_nome_arquivo_not_null NOT NULL nome_arquivo,
	CONSTRAINT arquivos_pkey PRIMARY KEY (id)
);


-- public.arquivos chaves estrangeiras

ALTER TABLE public.arquivos ADD CONSTRAINT fk_arquivo_contrato FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);