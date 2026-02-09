package com.example.checklistapp.task.dto;

import com.example.checklistapp.type.dto.TypeSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskSummaryDTO(

    Integer id,
    String nombre,
    LocalDateTime fechaCreacion,
    LocalDate fechaProgramacion,
    LocalDate fechaRealizacion,
    Boolean completada,
    Integer idUsuario,
    TypeSummaryDTO tipo

) {
}
