package com.example.checklistapp.dto.task;

import com.example.checklistapp.dto.type.TypeSummaryDTO;
import com.example.checklistapp.dto.user.UserSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO completo para respuesta de Task.
 *
 * <p>Incluye:
 * <ul>
 *   <li>Todos los campos de la entidad</li>
 *   <li>Objetos anidados (UserSummaryDTO, TypeSummaryDTO)</li>
 *   <li>Campos derivados si fueran necesarios</li>
 * </ul>
 *
 * <p>Requiere que el TaskRepository tenga método con JOIN FETCH para evitar
 * {@link org.hibernate.LazyInitializationException}:
 *
 * <pre>
 * {@literal @}Query("SELECT t FROM Task t " +
 *        "LEFT JOIN FETCH t.usuario " +
 *        "LEFT JOIN FETCH t.tipo " +
 *        "WHERE t.id = :id")
 * Optional{@literal <Task>} findByIdWithUsuarioAndTipo({@literal @}Param("id") Integer id);
 * </pre>
 *
 * <p>Uso con MapStruct:
 * <pre>
 * {@literal @}Mapping(target = "usuario", source = "usuario")
 * {@literal @}Mapping(target = "tipo", source = "tipo")
 * TaskResponseDTO toResponseDTO(Task task);
 * </pre>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record TaskResponseDTO(

    // Campos propios de Task
    Integer id,
    String nombre,
    String descripcion,
    LocalDateTime fechaCreacion,
    LocalDate fechaProgramacion,
    LocalDate fechaRealizacion,
    Boolean completada,

    // Objeto anidado (siempre presente, una tarea SIEMPRE tiene usuario)
    UserSummaryDTO usuario,

    // Objeto anidado opcional (puede ser NULL)
    TypeSummaryDTO tipo  // NULL si la tarea no tiene tipo

) {

    /**
     * Factory method para crear el DTO desde los componentes.
     * <p>Este método será usado por MapStruct, que llamará a este constructor
     * después de mapear Task → UserSummaryDTO y Task → TypeSummaryDTO.
     *
     * @param id ID de la tarea
     * @param nombre Nombre de la tarea
     * @param descripcion Descripción de la tarea
     * @param fechaCreacion Fecha de creación
     * @param fechaProgramacion Fecha de programación
     * @param fechaRealizacion Fecha de realización
     * @param completada Estado de completitud
     * @param usuario Usuario asociado
     * @param tipo Tipo asociado (puede ser null)
     * @return TaskResponseDTO completamente poblado
     */
    public static TaskResponseDTO of(
        Integer id,
        String nombre,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDate fechaProgramacion,
        LocalDate fechaRealizacion,
        Boolean completada,
        UserSummaryDTO usuario,
        TypeSummaryDTO tipo
    ) {
        return new TaskResponseDTO(
            id, nombre, descripcion, fechaCreacion,
            fechaProgramacion, fechaRealizacion, completada,
            usuario, tipo
        );
    }

    /**
     * Helper para UI: indica si la tarea está vencida.
     *
     * <p>Regla de negocio:
     * <ul>
     *   <li>No completada Y fechaProgramacion {@literal <} hoy → vencida</li>
     *   <li>Completada → no vencida (ya se hizo)</li>
     *   <li>Sin fechaProgramacion → no vencida</li>
     * </ul>
     *
     * @return true si la tarea está vencida según las reglas de negocio
     */
    public boolean estaVencida() {
        if (completada || fechaProgramacion == null) {
            return false;
        }
        return fechaProgramacion.isBefore(LocalDate.now());
    }

    /**
     * Helper para UI: indica si la tarea está programada para hoy.
     *
     * @return true si fechaProgramacion es hoy
     */
    public boolean estaProgramadaParaHoy() {
        return fechaProgramacion != null
            && fechaProgramacion.equals(LocalDate.now());
    }

    /**
     * Helper para UI: indica si la tarea fue completada tarde.
     *
     * @return true si completada=true y fechaRealizacion > fechaProgramacion
     */
    public boolean fueCompletadaTarde() {
        if (!completada || fechaRealizacion == null || fechaProgramacion == null) {
            return false;
        }
        return fechaRealizacion.isAfter(fechaProgramacion);
    }
}
