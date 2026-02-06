package com.example.checklistapp.dto.task;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para actualizar una Task existente.
 *
 * <p>Estrategia de actualización: "null = no actualizar este campo"
 *
 * <p>Importante:
 * <ul>
 *   <li>Para REMOVER el tipo: enviar {@code {"idTipo": null}}</li>
 *   <li>Para NO cambiar el tipo: no enviar el campo idTipo</li>
 * </ul>
 *
 * <p>Esto requiere que el Service use MapStruct con:
 * <pre>
 * {@literal @}Mapping(target = "idTipo", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
 * </pre>
 *
 * <p>O validación manual en Service:
 * <pre>
 * if (dto.idTipo() != null) {
 *     task.setIdTipo(dto.idTipo());  // Permite setear null
 * }
 * </pre>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record UpdateTaskDTO(

    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    Boolean completada,

    @Future(message = "La fecha de programación debe ser futura")
    LocalDate fechaProgramacion,

    LocalDate fechaRealizacion,

    Integer idTipo  // NULL permite remover el tipo existente

) {

    /**
     * Helper para saber si el usuario quiere cambiar al menos un campo.
     * <p>Evita transacciones DB innecesarias si todos los campos son null.
     *
     * @return true si al menos un campo no es null
     */
    public boolean tieneCambios() {
        return nombre != null
            || descripcion != null
            || completada != null
            || fechaProgramacion != null
            || fechaRealizacion != null
            || idTipo != null;
    }

    /**
     * Helper para detectar cambio de estado (pending → completed o viceversa).
     * <p>Útil para disparar side-effects (enviar email, actualizar métricas, etc).
     *
     * @param estadoActual el estado actual de completada de la tarea
     * @return true si el DTO tiene un valor diferente de completada
     */
    public boolean cambiaCompletada(Boolean estadoActual) {
        return completada != null && !completada.equals(estadoActual);
    }

    /**
     * Helper para saber si el usuario está marcando la tarea como completada.
     *
     * @return true si completada es true
     */
    public boolean seMarcaComoCompletada() {
        return Boolean.TRUE.equals(completada);
    }

    /**
     * Helper para saber si el usuario está desmarcando la tarea como completada.
     *
     * @return true si completada es false
     */
    public boolean seDesmarcaComoCompletada() {
        return Boolean.FALSE.equals(completada);
    }
}
