package com.example.checklistapp.task.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskDTO(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    @NotNull(message = "El usuario es obligatorio")
    Integer idUsuario,

    Integer idTipo,

    @Future(message = "La fecha de programación debe ser futura")
    LocalDate fechaProgramacion,

    Boolean completada,

    LocalDate fechaRealizacion

) {

    public boolean tieneFechaRealizacionExplicita() {
        return fechaRealizacion != null;
    }

    public boolean estaMarcadaComoCompletada() {
        return Boolean.TRUE.equals(completada);
    }
}
