-- Índices para optimizar consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_tareas_usuario ON gestor_tareas.tareas(id_usuario);
CREATE INDEX IF NOT EXISTS idx_tareas_tipo ON gestor_tareas.tareas(id_tipo);
CREATE INDEX IF NOT EXISTS idx_tareas_fecha_programacion ON gestor_tareas.tareas(fecha_programacion);
CREATE INDEX IF NOT EXISTS idx_tareas_completada ON gestor_tareas.tareas(completada);
CREATE INDEX IF NOT EXISTS idx_tipos_usuario ON gestor_tareas.tipos(id_usuario);
