-- Hacer id_tipo opcional en la tabla tareas para soportar tareas sin tipo
-- Autor: Claude Code
-- Fecha: 2026-02-26

-- Modificar la columna id_tipo para permitir NULL
ALTER TABLE gestor_tareas.tareas
ALTER COLUMN id_tipo DROP NOT NULL;

-- Comentario sobre el cambio
COMMENT ON COLUMN gestor_tareas.tareas.id_tipo IS 'ID del tipo de tarea (opcional, NULL para tareas sin clasificar)';
