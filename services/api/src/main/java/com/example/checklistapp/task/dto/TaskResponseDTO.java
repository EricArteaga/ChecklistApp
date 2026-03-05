package com.example.checklistapp.task.dto;

import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponseDTO(

    Integer id,
    String nombre,
    String descripcion,
    LocalDateTime fechaCreacion,
    LocalDate fechaProgramacion,
    LocalDate fechaRealizacion,
    Boolean completada,

    UserSummaryDTO usuario,

    TypeSummaryDTO tipo,

    List<SubitemResponseDTO> subitems

) {

    public static TaskResponseDTO of(
        Integer id,
        String nombre,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDate fechaProgramacion,
        LocalDate fechaRealizacion,
        Boolean completada,
        UserSummaryDTO usuario,
        TypeSummaryDTO tipo,
        List<SubitemResponseDTO> subitems
    ) {
        return new TaskResponseDTO(
            id, nombre, descripcion, fechaCreacion,
            fechaProgramacion, fechaRealizacion, completada,
            usuario, tipo, subitems
        );
    }

    public boolean estaVencida() {
        if (completada || fechaProgramacion == null) {
            return false;
        }
        return fechaProgramacion.isBefore(LocalDate.now());
    }

    public boolean estaProgramadaParaHoy() {
        return fechaProgramacion != null
            && fechaProgramacion.equals(LocalDate.now());
    }

    public boolean fueCompletadaTarde() {
        if (!completada || fechaRealizacion == null || fechaProgramacion == null) {
            return false;
        }
        return fechaRealizacion.isAfter(fechaProgramacion);
    }

    public long getSubitemsCompletados() {
        if (subitems == null) {
            return 0;
        }
        return subitems.stream().filter(SubitemResponseDTO::checked).count();
    }

    public boolean estaCompletaConSubitems() {
        if (subitems == null || subitems.isEmpty()) {
            return completada;
        }
        return subitems.stream().allMatch(SubitemResponseDTO::checked);
    }
}
