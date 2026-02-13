-- Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS gestor_tareas;

-- Tabla: usuarios
CREATE TABLE IF NOT EXISTS gestor_tareas.usuarios (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correo VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    hash_contrasena VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_correo_valido CHECK (correo ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')
);

COMMENT ON TABLE gestor_tareas.usuarios IS 'Tabla para almacenar información de usuarios.';
COMMENT ON COLUMN gestor_tareas.usuarios.correo IS 'Correo electrónico único del usuario.';
COMMENT ON COLUMN gestor_tareas.usuarios.nombre IS 'Nombre del usuario.';
COMMENT ON COLUMN gestor_tareas.usuarios.hash_contrasena IS 'Hash de la contraseña del usuario (hashear en la aplicación).';
COMMENT ON COLUMN gestor_tareas.usuarios.fecha_creacion IS 'Fecha y hora de creación del usuario.';

-- Tabla: tipos
CREATE TABLE IF NOT EXISTS gestor_tareas.tipos (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    color VARCHAR(50),
    CONSTRAINT fk_tipos_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES gestor_tareas.usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_color_valido CHECK (color ~ '^#[0-9A-Fa-f]{6}$|^#[0-9A-Fa-f]{3}$|NULL'),
    UNIQUE (id_usuario, nombre)
);

COMMENT ON TABLE gestor_tareas.tipos IS 'Tabla de catálogo para tipos de tareas, específicos por usuario.';
COMMENT ON COLUMN gestor_tareas.tipos.id_usuario IS 'ID del usuario propietario del tipo.';
COMMENT ON COLUMN gestor_tareas.tipos.nombre IS 'Nombre del tipo de tarea (único por usuario).';
COMMENT ON COLUMN gestor_tareas.tipos.color IS 'Color asociado (formato HEX o NULL).';

-- Tabla: tareas
CREATE TABLE IF NOT EXISTS gestor_tareas.tareas (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_tipo INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    completada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_programacion DATE,
    fecha_realizacion DATE,
    CONSTRAINT fk_tareas_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES gestor_tareas.usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tareas_tipo
        FOREIGN KEY (id_tipo)
        REFERENCES gestor_tareas.tipos(id)
        ON DELETE NO ACTION,
    CONSTRAINT chk_fechas_logicas CHECK (
        (fecha_programacion IS NULL OR fecha_realizacion IS NULL OR fecha_realizacion >= fecha_programacion) AND
        (completada = TRUE IMPLIES fecha_realizacion IS NOT NULL)
    )
);

COMMENT ON TABLE gestor_tareas.tareas IS 'Tabla para almacenar tareas de usuarios.';
COMMENT ON COLUMN gestor_tareas.tareas.id_usuario IS 'ID del usuario propietario.';
COMMENT ON COLUMN gestor_tareas.tareas.id_tipo IS 'ID del tipo de tarea (debe pertenecer al mismo usuario).';
COMMENT ON COLUMN gestor_tareas.tareas.nombre IS 'Nombre de la tarea.';
COMMENT ON COLUMN gestor_tareas.tareas.descripcion IS 'Descripción detallada de la tarea (opcional).';
COMMENT ON COLUMN gestor_tareas.tareas.completada IS 'Indica si la tarea está completada.';
COMMENT ON COLUMN gestor_tareas.tareas.fecha_creacion IS 'Fecha y hora de creación de la tarea.';
COMMENT ON COLUMN gestor_tareas.tareas.fecha_programacion IS 'Fecha programada para la tarea.';
COMMENT ON COLUMN gestor_tareas.tareas.fecha_realizacion IS 'Fecha de realización de la tarea.';
