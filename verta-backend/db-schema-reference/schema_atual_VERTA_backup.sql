-- Schema extraido de VERTA_backup.sql (dump enviado por quem esta cuidando do banco)
-- Gerado automaticamente para referencia - nao editar a mao.

-- TABLE: arquivos
CREATE TABLE public.arquivos (
    id integer NOT NULL,
    contrato_id integer NOT NULL,
    nome_arquivo character varying(255) NOT NULL,
    caminho_arquivo character varying(500) NOT NULL,
    tipo_arquivo character varying(100),
    tamanho bigint,
    data_armazenamento timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    versao_id integer NOT NULL
);

-- TABLE: contrato_usuarios
CREATE TABLE public.contrato_usuarios (
    id integer NOT NULL,
    contrato_id integer NOT NULL,
    usuario_id integer NOT NULL,
    pode_visualizar boolean DEFAULT true,
    pode_editar boolean DEFAULT false,
    pode_assinar boolean DEFAULT false,
    pode_excluir boolean DEFAULT false,
    data_vinculo timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    empresa_id integer
);

-- TABLE: contratos
CREATE TABLE public.contratos (
    id integer NOT NULL,
    empresa_id integer NOT NULL,
    template_id integer,
    titulo character varying(200) NOT NULL,
    tipo character varying(100),
    status character varying(50) DEFAULT 'RASCUNHO'::character varying NOT NULL,
    criado_por integer NOT NULL,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT contratos_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'EM_REVISAO'::character varying, 'FINALIZADO'::character varying, 'ARQUIVADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

-- TABLE: empresas
CREATE TABLE public.empresas (
    id integer NOT NULL,
    nome character varying(150) NOT NULL,
    cnpj character varying(18),
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- TABLE: templates
CREATE TABLE public.templates (
    id integer NOT NULL,
    empresa_id integer NOT NULL,
    nome character varying(150) NOT NULL,
    descricao text,
    conteudo text NOT NULL,
    ativo boolean DEFAULT true,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- TABLE: usuarios
CREATE TYPE public.tipo_perfil AS ENUM (
    'ADMIN',
    'COMUM'
);

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    empresa_id integer NOT NULL,
    nome character varying(150) NOT NULL,
    email character varying(150) NOT NULL,
    senha character varying(255) NOT NULL,
    perfil public.tipo_perfil NOT NULL,
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    token_sessao character varying(255)
);

-- TABLE: versoes_contrato
CREATE TABLE public.versoes_contrato (
    id integer NOT NULL,
    contrato_id integer NOT NULL,
    numero_versao integer NOT NULL,
    conteudo text NOT NULL,
    criado_por integer NOT NULL,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- SEQUENCE: arquivos_id_seq
CREATE SEQUENCE public.arquivos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: contrato_usuarios_id_seq
CREATE SEQUENCE public.contrato_usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: contratos_id_seq
CREATE SEQUENCE public.contratos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: empresas_id_seq
CREATE SEQUENCE public.empresas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: templates_id_seq
CREATE SEQUENCE public.templates_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: usuarios_id_seq
CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- SEQUENCE: versoes_contrato_id_seq
CREATE SEQUENCE public.versoes_contrato_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- CONSTRAINT: arquivos arquivos_pkey
ALTER TABLE ONLY public.arquivos
    ADD CONSTRAINT arquivos_pkey PRIMARY KEY (id);

-- CONSTRAINT: contrato_usuarios contrato_usuarios_pkey
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT contrato_usuarios_pkey PRIMARY KEY (id);

-- CONSTRAINT: contratos contratos_pkey
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_pkey PRIMARY KEY (id);

-- CONSTRAINT: empresas empresas_cnpj_key
ALTER TABLE ONLY public.empresas
    ADD CONSTRAINT empresas_cnpj_key UNIQUE (cnpj);

-- CONSTRAINT: empresas empresas_pkey
ALTER TABLE ONLY public.empresas
    ADD CONSTRAINT empresas_pkey PRIMARY KEY (id);

-- CONSTRAINT: templates templates_pkey
ALTER TABLE ONLY public.templates
    ADD CONSTRAINT templates_pkey PRIMARY KEY (id);

-- CONSTRAINT: contratos uk_contrato_empresa
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT uk_contrato_empresa UNIQUE (id, empresa_id);

-- CONSTRAINT: contrato_usuarios uk_contrato_usuario
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT uk_contrato_usuario UNIQUE (contrato_id, usuario_id);

-- CONSTRAINT: versoes_contrato uk_contrato_versao
ALTER TABLE ONLY public.versoes_contrato
    ADD CONSTRAINT uk_contrato_versao UNIQUE (contrato_id, numero_versao);

-- CONSTRAINT: templates uk_template_empresa
ALTER TABLE ONLY public.templates
    ADD CONSTRAINT uk_template_empresa UNIQUE (id, empresa_id);

-- CONSTRAINT: usuarios uk_usuario_empresa
ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_usuario_empresa UNIQUE (id, empresa_id);

-- CONSTRAINT: usuarios usuarios_email_key
ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);

-- CONSTRAINT: usuarios usuarios_pkey
ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);

-- CONSTRAINT: versoes_contrato versoes_contrato_pkey
ALTER TABLE ONLY public.versoes_contrato
    ADD CONSTRAINT versoes_contrato_pkey PRIMARY KEY (id);

-- INDEX: idx_arquivos_contrato
CREATE INDEX idx_arquivos_contrato ON public.arquivos USING btree (contrato_id);

-- INDEX: idx_arquivos_versao
CREATE INDEX idx_arquivos_versao ON public.arquivos USING btree (versao_id);

-- INDEX: idx_contratos_empresa
CREATE INDEX idx_contratos_empresa ON public.contratos USING btree (empresa_id);

-- INDEX: idx_templates_empresa
CREATE INDEX idx_templates_empresa ON public.templates USING btree (empresa_id);

-- INDEX: idx_usuarios_empresa
CREATE INDEX idx_usuarios_empresa ON public.usuarios USING btree (empresa_id);

-- INDEX: idx_versoes_contrato_numero
CREATE INDEX idx_versoes_contrato_numero ON public.versoes_contrato USING btree (contrato_id, numero_versao DESC);

-- FK CONSTRAINT: contrato_usuarios fk_acesso_contrato_empresa
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT fk_acesso_contrato_empresa FOREIGN KEY (contrato_id, empresa_id) REFERENCES public.contratos(id, empresa_id);

-- FK CONSTRAINT: contrato_usuarios fk_acesso_usuario_empresa
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT fk_acesso_usuario_empresa FOREIGN KEY (usuario_id, empresa_id) REFERENCES public.usuarios(id, empresa_id);

-- FK CONSTRAINT: arquivos fk_arquivo_contrato
ALTER TABLE ONLY public.arquivos
    ADD CONSTRAINT fk_arquivo_contrato FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);

-- FK CONSTRAINT: arquivos fk_arquivo_versao
ALTER TABLE ONLY public.arquivos
    ADD CONSTRAINT fk_arquivo_versao FOREIGN KEY (versao_id) REFERENCES public.versoes_contrato(id);

-- FK CONSTRAINT: contratos fk_contrato_empresa
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);

-- FK CONSTRAINT: contratos fk_contrato_template_empresa
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_template_empresa FOREIGN KEY (template_id, empresa_id) REFERENCES public.templates(id, empresa_id);

-- FK CONSTRAINT: contrato_usuarios fk_contrato_usuario_acesso
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT fk_contrato_usuario_acesso FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);

-- FK CONSTRAINT: contratos fk_contrato_usuario_empresa
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_usuario_empresa FOREIGN KEY (criado_por, empresa_id) REFERENCES public.usuarios(id, empresa_id);

-- FK CONSTRAINT: templates fk_template_empresa
ALTER TABLE ONLY public.templates
    ADD CONSTRAINT fk_template_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);

-- FK CONSTRAINT: contrato_usuarios fk_usuario_contrato_acesso
ALTER TABLE ONLY public.contrato_usuarios
    ADD CONSTRAINT fk_usuario_contrato_acesso FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);

-- FK CONSTRAINT: usuarios fk_usuario_empresa
ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES public.empresas(id);

-- FK CONSTRAINT: versoes_contrato fk_versao_contrato
ALTER TABLE ONLY public.versoes_contrato
    ADD CONSTRAINT fk_versao_contrato FOREIGN KEY (contrato_id) REFERENCES public.contratos(id);

-- FK CONSTRAINT: versoes_contrato fk_versao_usuario
ALTER TABLE ONLY public.versoes_contrato
    ADD CONSTRAINT fk_versao_usuario FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);

