package com.example.checklistapp.task.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskDTO(

    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    Boolean completada,

    @Future(message = "La fecha de programación debe ser futura")
    LocalDate fechaProgramacion,

    LocalDate fechaRealizacion,

    Integer idTipo

) {

    public boolean tieneCambios() {
        return nombre != null
            || descripcion != null
            || completada != null
            || fechaProgramacion != null
            || fechaRealizacion != null
            || idTipo != null;
    }

    public boolean cambiaCompletada(Boolean estadoActual) {
        return completada != null && !completada.equals(estadoActual);
    }

    public boolean seMarcaComoCompletada() {
        return Boolean.TRUE.equals(completada);
    }

    public boolean seDesmarcaComoCompletada() {
        return Boolean.FALSE.equals(completada);
    }
}
