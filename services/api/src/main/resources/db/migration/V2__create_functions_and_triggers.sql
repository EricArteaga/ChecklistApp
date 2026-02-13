-- Función: Crear tipo 'Sin tipo' automáticamente al insertar un usuario
CREATE OR REPLACE FUNCTION gestor_tareas.create_default_tipo() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO gestor_tareas.tipos (id_usuario, nombre, color)
    VALUES (NEW.id, 'Sin tipo', NULL);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Crear tipo 'Sin tipo' automáticamente
DROP TRIGGER IF EXISTS trg_create_default_tipo ON gestor_tareas.usuarios;
CREATE TRIGGER trg_create_default_tipo
AFTER INSERT ON gestor_tareas.usuarios
FOR EACH ROW EXECUTE FUNCTION gestor_tareas.create_default_tipo();

-- Función: Reasignar tareas a 'Sin tipo' antes de eliminar un tipo
CREATE OR REPLACE FUNCTION gestor_tareas.reassign_tareas_on_delete_tipo() RETURNS TRIGGER AS $$
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

-- Trigger: Reasignar tareas antes de eliminar tipo
DROP TRIGGER IF EXISTS trg_reassign_tareas_on_delete_tipo ON gestor_tareas.tipos;
CREATE TRIGGER trg_reassign_tareas_on_delete_tipo
BEFORE DELETE ON gestor_tareas.tipos
FOR EACH ROW EXECUTE FUNCTION gestor_tareas.reassign_tareas_on_delete_tipo();

-- Función: Verificar que el tipo pertenezca al mismo usuario que la tarea
CREATE OR REPLACE FUNCTION gestor_tareas.check_tipo_usuario() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id_usuario != (SELECT id_usuario FROM gestor_tareas.tipos WHERE id = NEW.id_tipo) THEN
        RAISE EXCEPTION 'El tipo debe pertenecer al mismo usuario que la tarea.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Verificar tipo de tarea pertenece al usuario
DROP TRIGGER IF EXISTS trg_check_tipo_usuario ON gestor_tareas.tareas;
CREATE TRIGGER trg_check_tipo_usuario
BEFORE INSERT OR UPDATE ON gestor_tareas.tareas
FOR EACH ROW EXECUTE FUNCTION gestor_tareas.check_tipo_usuario();
