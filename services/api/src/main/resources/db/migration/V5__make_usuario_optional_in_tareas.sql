-- Hacer id_usuario opcional en la tabla tareas para soportar tareas anónimas
-- Autor: Agent Coordinator
-- Fecha: 2026-02-26

-- Modificar la columna id_usuario para permitir NULL
ALTER TABLE gestor_tareas.tareas
ALTER COLUMN id_usuario DROP NOT NULL;

-- Comentario sobre el cambio
COMMENT ON COLUMN gestor_tareas.tareas.id_usuario IS 'ID del usuario (opcional, NULL para tareas anónimas)';
