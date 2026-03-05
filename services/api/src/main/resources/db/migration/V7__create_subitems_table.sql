-- Tabla: subitems
-- Subitems o checklist items dentro de una tarea
CREATE TABLE gestor_tareas.subitems (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tarea INT NOT NULL,
    description VARCHAR(500) NOT NULL,
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subitems_tarea
        FOREIGN KEY (id_tarea)
        REFERENCES gestor_tareas.tareas(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_description_not_empty CHECK (trim(description) != '')
);

COMMENT ON TABLE gestor_tareas.subitems IS 'Subitems o checklist items dentro de una tarea.';
COMMENT ON COLUMN gestor_tareas.subitems.id_tarea IS 'ID de la tarea padre.';
COMMENT ON COLUMN gestor_tareas.subitems.description IS 'Descripción del subitem.';
COMMENT ON COLUMN gestor_tareas.subitems.checked IS 'Indica si el subitem está completado.';
COMMENT ON COLUMN gestor_tareas.subitems.fecha_creacion IS 'Fecha y hora de creación del subitem.';

-- Índice para búsquedas por tarea
CREATE INDEX idx_subitems_tarea ON gestor_tareas.subitems(id_tarea);
