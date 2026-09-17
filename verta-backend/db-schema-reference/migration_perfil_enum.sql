-- Migracao: perfil vira ENUM nativo (public.tipo_perfil)
-- Atencao: o USING abaixo falha se existir alguma linha com valor de perfil
-- diferente de 'ADMIN' ou 'COMUM' (ex: valores antigos como ADMINISTRADOR,
-- EDITOR, VISUALIZADOR) - normalize esses dados antes de rodar.

CREATE TYPE public.tipo_perfil AS ENUM (
    'ADMIN',
    'COMUM'
);

ALTER TABLE public.usuarios
ALTER COLUMN perfil TYPE public.tipo_perfil
USING perfil::public.tipo_perfil;
