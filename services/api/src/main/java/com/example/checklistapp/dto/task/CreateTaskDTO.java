package com.example.checklistapp.dto.task;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para crear una nueva Task.
 *
 * <p>Validaciones Jakarta:
 * <ul>
 *   <li>nombre: obligatorio, max 150 caracteres</li>
 *   <li>descripcion: opcional, max 1000 caracteres</li>
 *   <li>idUsuario: obligatorio</li>
 *   <li>idTipo: opcional (nullable)</li>
 *   <li>fechaProgramacion: opcional, debe ser futura si se proporciona</li>
 * </ul>
 *
 * <p>Casos de uso:
 * <ul>
 *   <li>Crear tarea pendiente: {@code {completada: false}}</li>
 *   <li>Crear tarea ya completada: {@code {completada: true, fechaRealizacion: "2026-02-01"}}</li>
 *   <li>Crear tarea completada sin fecha: {@code {completada: true}} → Service usara LocalDate.now()</li>
 * </ul>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record CreateTaskDTO(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    @NotNull(message = "El usuario es obligatorio")
    Integer idUsuario,

    Integer idTipo,  // NULL-able, sin @NotNull

    @Future(message = "La fecha de programación debe ser futura")
    LocalDate fechaProgramacion,

    Boolean completada,  // NULL-able, default false en Service

    LocalDate fechaRealizacion  // NULL-able, Service usa LocalDate.now() si completada=true

) {

    /**
     * Helper para saber si el usuario proporcionó fecha de realización explícita.
     * <p>Evita la ambigüedad de "null significa lo mismo que no enviarse".
     *
     * @return true si el usuario envió una fecha de realización explícita
     */
    public boolean tieneFechaRealizacionExplicita() {
        return fechaRealizacion != null;
    }

    /**
     * Helper para saber si el usuario está marcando la tarea como completada.
     *
     * @return true si completada es true (o null, que el Service interpretará como false)
     */
    public boolean estaMarcadaComoCompletada() {
        return Boolean.TRUE.equals(completada);
    }
}
