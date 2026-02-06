package com.example.checklistapp.dto.type;

/**
 * DTO resumido de Type (tipo/categoría de Task).
 *
 * <p>Casos de uso:
 * <ul>
 *   <li>Anidado en TaskResponseDTO (puede ser NULL)</li>
 *   <li>Anidado en TaskSummaryDTO (puede ser NULL)</li>
 * </ul>
 *
 * <p>Campo opcional:
 * <ul>
 *   <li>descripcion: solo si la UI necesita mostrar tooltip con descripción del tipo</li>
 * </ul>
 *
 * <p><b>Null-safety:</b>
 * <ul>
 *   <li>Task puede no tener tipo asociado (idTipo = NULL)</li>
 *   <li>TaskResponseDTO.tipo puede ser NULL</li>
 *   <li>Frontend debe manejar esto: {@code task.tipo?.nombre ?? "Sin tipo"}</li>
 * </ul>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record TypeSummaryDTO(

    Integer id,
    String nombre,
    String color,  // Color HEX para UI (ej: "#FF5733")
    String descripcion  // NULL-able

) {

    /**
     * Factory method para crear el DTO desde una entidad Type.
     *
     * @param id ID del tipo
     * @param nombre Nombre del tipo
     * @param color Color HEX del tipo
     * @param descripcion Descripción del tipo (opcional)
     * @return TypeSummaryDTO completamente poblado
     */
    public static TypeSummaryDTO of(Integer id, String nombre, String color, String descripcion) {
        return new TypeSummaryDTO(id, nombre, color, descripcion);
    }

    /**
     * Factory method simplificado sin descripción.
     *
     * @param id ID del tipo
     * @param nombre Nombre del tipo
     * @param color Color HEX del tipo
     * @return TypeSummaryDTO sin descripción
     */
    public static TypeSummaryDTO of(Integer id, String nombre, String color) {
        return new TypeSummaryDTO(id, nombre, color, null);
    }

    /**
     * Helper para UI: indica si el color es válido.
     *
     * @return true si el color es un HEX válido
     */
    public boolean tieneColorValido() {
        return color != null && color.matches("^#[0-9A-Fa-f]{6}$");
    }
}
