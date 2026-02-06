-- =========================================================
-- Base de datos: Gestor de Tareas
-- Motor: PostgreSQL
--
-- Decisiones de diseño:
-- - Se usa GENERATED AS IDENTITY para los IDs (forma moderna en PostgreSQL)
-- - BOOLEAN es un tipo nativo (true / false)
-- - TIMESTAMP se usa para fecha de creación
-- - DATE se usa para fechas de planificación y realización
-- - Esquema dedicado 'gestor_tareas' para mejor organización
-- - Validaciones adicionales para datos (correo, color, fechas lógicas)
-- - Campos extra para extensibilidad mínima en MVP (fecha_creacion en usuarios, descripcion en tareas)
-- - Tipos son específicos por usuario: columna id_usuario en tipos con FK y CASCADE
-- - UNIQUE en (id_usuario, nombre) de tipos para evitar duplicados por usuario
-- - Trigger para asegurar que las tareas usen tipos del mismo usuario
-- - Trigger para crear tipo 'Sin tipo' automáticamente al insertar usuario
-- - Trigger BEFORE DELETE en tipos para reasignar tareas a 'Sin tipo' antes de eliminar (permite eliminación incluso con tareas asociadas)
-- - FK en tareas cambiada a ON DELETE NO ACTION para permitir el manejo por trigger
-- - Índices adicionales para consultas comunes en checklists y heatmaps (por fechas y completadas)
-- =========================================================

-- Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS gestor_tareas;
SET search_path TO gestor_tareas;

-- =========================================================
-- Tabla: usuarios
--
-- Un usuario puede tener muchas tareas y tipos.
-- Si se elimina un usuario, se eliminan automáticamente
-- todas sus tareas y tipos (ON DELETE CASCADE en las FK correspondientes).
-- =========================================================
CREATE TABLE usuarios (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correo VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    hash_contrasena VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_correo_valido CHECK (correo ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')
);

COMMENT ON TABLE usuarios IS 'Tabla para almacenar información de usuarios.';
COMMENT ON COLUMN usuarios.correo IS 'Correo electrónico único del usuario.';
COMMENT ON COLUMN usuarios.nombre IS 'Nombre del usuario.';
COMMENT ON COLUMN usuarios.hash_contrasena IS 'Hash de la contraseña del usuario (hashear en la aplicación).';
COMMENT ON COLUMN usuarios.fecha_creacion IS 'Fecha y hora de creación del usuario.';

-- =========================================================
-- Trigger: Crear tipo 'Sin tipo' automáticamente al insertar un usuario
-- =========================================================
CREATE OR REPLACE FUNCTION create_default_tipo() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO gestor_tareas.tipos (id_usuario, nombre, color)
    VALUES (NEW.id, 'Sin tipo', NULL);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_default_tipo
AFTER INSERT ON gestor_tareas.usuarios
FOR EACH ROW EXECUTE FUNCTION create_default_tipo();

-- =========================================================
-- Tabla: tipos
--
-- Tabla de catálogo para clasificar tareas, específica por usuario.
-- Si se elimina un tipo (excepto 'Sin tipo'), las tareas asociadas se reasignan a 'Sin tipo' via trigger.
-- Si se elimina un usuario, se eliminan sus tipos (ON DELETE CASCADE).
-- =========================================================
CREATE TABLE tipos (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    color VARCHAR(50),
    CONSTRAINT fk_tipos_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_color_valido CHECK (color ~ '^#[0-9A-Fa-f]{6}$|^#[0-9A-Fa-f]{3}$|NULL'),
    UNIQUE (id_usuario, nombre)
);

COMMENT ON TABLE tipos IS 'Tabla de catálogo para tipos de tareas, específicos por usuario.';
COMMENT ON COLUMN tipos.id_usuario IS 'ID del usuario propietario del tipo.';
COMMENT ON COLUMN tipos.nombre IS 'Nombre del tipo de tarea (único por usuario).';
COMMENT ON COLUMN tipos.color IS 'Color asociado (formato HEX o NULL).';

-- =========================================================
-- Trigger: Reasignar tareas a 'Sin tipo' antes de eliminar un tipo
-- =========================================================
CREATE OR REPLACE FUNCTION reassign_tareas_on_delete_tipo() RETURNS TRIGGER AS $$
DECLARE
    default_tipo_id INT;
BEGIN
    IF OLD.nombre = 'Sin tipo' THEN
        RAISE EXCEPTION 'No se puede eliminar el tipo predeterminado ''Sin tipo''.';
    END IF;

    SELECT id INTO default_tipo_id
    FROM gestor_tareas.tipos
    WHERE id_usuario = OLD.id_usuario AND nombre = 'Sin tipo';

    IF default_tipo_id IS NULL THEN
        RAISE EXCEPTION 'No existe tipo ''Sin tipo'' para el usuario %.', OLD.id_usuario;
    END IF;

    UPDATE gestor_tareas.tareas
    SET id_tipo = default_tipo_id
    WHERE id_tipo = OLD.id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reassign_tareas_on_delete_tipo
BEFORE DELETE ON gestor_tareas.tipos
FOR EACH ROW EXECUTE FUNCTION reassign_tareas_on_delete_tipo();

-- =========================================================
-- Tabla: tareas
--
-- Relaciones:
-- - Cada tarea pertenece a un usuario (FK a usuarios)
-- - Cada tarea tiene un tipo (FK a tipos), que debe pertenecer al mismo usuario (verificado por trigger)
--
-- Reglas:
-- - completada es BOOLEAN (true / false)
-- - fecha_creacion se guarda como TIMESTAMP
-- - fecha_programacion y fecha_realizacion se guardan como DATE
-- =========================================================
CREATE TABLE tareas (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- Relación con usuarios
    id_usuario INT NOT NULL,
    -- Relación con tipos
    id_tipo INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    -- BOOLEAN es un tipo nativo en PostgreSQL
    completada BOOLEAN NOT NULL DEFAULT FALSE,
    -- Fecha y hora exacta de creación
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Fechas sin hora (planificación y realización)
    fecha_programacion DATE,
    fecha_realizacion DATE,
    -- Si se elimina un usuario, se eliminan sus tareas
    CONSTRAINT fk_tareas_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,
    -- No restringir eliminación de tipos (manejado por trigger)
    CONSTRAINT fk_tareas_tipo
        FOREIGN KEY (id_tipo)
        REFERENCES tipos(id)
        ON DELETE NO ACTION,
    -- Validaciones lógicas para fechas
    CONSTRAINT chk_fechas_logicas CHECK (
        (fecha_programacion IS NULL OR fecha_realizacion IS NULL OR fecha_realizacion >= fecha_programacion) AND
        (completada = FALSE OR fecha_realizacion IS NOT NULL)
    )
);

COMMENT ON TABLE tareas IS 'Tabla para almacenar tareas de usuarios.';
COMMENT ON COLUMN tareas.id_usuario IS 'ID del usuario propietario.';
COMMENT ON COLUMN tareas.id_tipo IS 'ID del tipo de tarea (debe pertenecer al mismo usuario).';
COMMENT ON COLUMN tareas.nombre IS 'Nombre de la tarea.';
COMMENT ON COLUMN tareas.descripcion IS 'Descripción detallada de la tarea (opcional).';
COMMENT ON COLUMN tareas.completada IS 'Indica si la tarea está completada.';
COMMENT ON COLUMN tareas.fecha_creacion IS 'Fecha y hora de creación de la tarea.';
COMMENT ON COLUMN tareas.fecha_programacion IS 'Fecha programada para la tarea.';
COMMENT ON COLUMN tareas.fecha_realizacion IS 'Fecha de realización de la tarea.';

-- =========================================================
-- Trigger: Verificar que el tipo pertenezca al mismo usuario que la tarea
-- =========================================================
CREATE OR REPLACE FUNCTION check_tipo_usuario() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id_usuario != (SELECT id_usuario FROM gestor_tareas.tipos WHERE id = NEW.id_tipo) THEN
        RAISE EXCEPTION 'El tipo debe pertenecer al mismo usuario que la tarea.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_tipo_usuario
BEFORE INSERT OR UPDATE ON gestor_tareas.tareas
FOR EACH ROW EXECUTE FUNCTION check_tipo_usuario();

-- =========================================================
-- Índices recomendados para mejorar rendimiento en búsquedas
-- (especialmente para heatmaps por fechas y completadas en MVP con bajo volumen)
-- =========================================================
CREATE INDEX idx_tareas_usuario ON tareas(id_usuario);
CREATE INDEX idx_tareas_tipo ON tareas(id_tipo);
CREATE INDEX idx_tareas_fecha_programacion ON tareas(fecha_programacion);
CREATE INDEX idx_tareas_completada ON tareas(completada);
CREATE INDEX idx_tipos_usuario ON tipos(id_usuario);