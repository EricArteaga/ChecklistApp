-- Usuario de prueba: test@example.com
INSERT INTO gestor_tareas.usuarios (correo, nombre, hash_contrasena)
VALUES ('test@example.com', 'Usuario Test', '$2a$10$placeholderHash')
ON CONFLICT (correo) DO NOTHING;

-- Tipos de tarea por defecto para el usuario de prueba
-- Nota: El trigger crea automáticamente el tipo 'Sin tipo'
INSERT INTO gestor_tareas.tipos (id_usuario, nombre, color)
SELECT 1, 'Trabajo', '#326cc3'
WHERE NOT EXISTS (SELECT 1 FROM gestor_tareas.tipos WHERE nombre = 'Trabajo' AND id_usuario = 1);

INSERT INTO gestor_tareas.tipos (id_usuario, nombre, color)
SELECT 1, 'Personal', '#55a2d2'
WHERE NOT EXISTS (SELECT 1 FROM gestor_tareas.tipos WHERE nombre = 'Personal' AND id_usuario = 1);

INSERT INTO gestor_tareas.tipos (id_usuario, nombre, color)
SELECT 1, 'Salud', '#3cc1a2'
WHERE NOT EXISTS (SELECT 1 FROM gestor_tareas.tipos WHERE nombre = 'Salud' AND id_usuario = 1);

-- Tareas de ejemplo
INSERT INTO gestor_tareas.tareas (id_usuario, id_tipo, nombre, descripcion, completada, fecha_programacion)
SELECT 1, 1, 'Completar reporte', 'Reporte mensual de ventas', FALSE, CURRENT_DATE + INTERVAL '7 days'
WHERE NOT EXISTS (SELECT 1 FROM gestor_tareas.tareas WHERE nombre = 'Completar reporte' AND id_usuario = 1);

INSERT INTO gestor_tareas.tareas (id_usuario, id_tipo, nombre, descripcion, completada, fecha_programacion, fecha_realizacion)
SELECT 1, 2, 'Comprar víveres', 'Ir al supermercado', TRUE, CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM gestor_tareas.tareas WHERE nombre = 'Comprar víveres' AND id_usuario = 1);
