package com.example.checklistapp.dto.task;

import com.example.checklistapp.dto.type.TypeSummaryDTO;

import java.time.LocalDate;

/**
 * DTO simplificado para listados de Tasks.
 *
 * <p>Casos de uso:
 * <ul>
 *   <li>{@code GET /tasks?page=0&size=100} → retorna {@code List<TaskSummaryDTO>}</li>
 *   <li>Ahorro de ancho de banda: no incluye descripcion, fechaCreacion, emailUsuario</li>
 * </ul>
 *
 * <p>Métricas de ahorro (asumiendo 100 tareas):
 * <ul>
 *   <li>TaskResponseDTO: ~15 campos × 100 = 1500 campos</li>
 *   <li>TaskSummaryDTO: ~6 campos × 100 = 600 campos</li>
 *   <li>Ahorro: 60% menos datos transferidos</li>
 * </ul>
 *
 * <p>Requiere query en repository:
 * <pre>
 * {@literal @}Query("SELECT t FROM Task t " +
 *        "LEFT JOIN FETCH t.tipo " +
 *        "ORDER BY t.fechaCreacion DESC")
 * Page{@literal <Task>} findAllSummary(Pageable pageable);
 * </pre>
 *
 * <p><b>NOTA:</b> No trae usuario por rendimiento. Si necesitas nombreUsuario,
 * agrega el campo o usa un DTO intermedio.
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record TaskSummaryDTO(

    Integer id,
    String nombre,
    Boolean completada,
    LocalDate fechaProgramacion,
    LocalDate fechaRealizacion,

    TypeSummaryDTO tipo,  // NULL si no tiene tipo

    Integer idUsuario  // ID para navegación, no objeto completo

) {

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
     * Helper para UI: indica si la tarea está programada para esta semana.
     *
     * <p>"Esta semana" = hoy hasta domingo de esta semana.
     *
     * @return true si fechaProgramacion es esta semana
     */
    public boolean estaProgramadaParaEstaSemana() {
        if (fechaProgramacion == null) {
            return false;
        }

        var hoy = LocalDate.now();
        var diaSemana = hoy.getDayOfWeek().getValue();
        var domingo = hoy.plusDays(7 - diaSemana);

        return !fechaProgramacion.isBefore(hoy)
            && !fechaProgramacion.isAfter(domingo);
    }
}
