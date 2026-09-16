-- Migracao: sessao unica por usuario
-- Adiciona a coluna que guarda o identificador da sessao (JWT) ativa.
-- Um novo login sobrescreve o valor e invalida qualquer token anterior.

ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS token_sessao varchar(255) NULL;
