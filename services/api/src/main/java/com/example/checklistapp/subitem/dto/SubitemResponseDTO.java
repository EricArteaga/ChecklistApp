package com.example.checklistapp.subitem.dto;

import java.time.LocalDateTime;

public record SubitemResponseDTO(
        Integer id,
        Integer idTarea,
        String description,
        Boolean checked,
        LocalDateTime fechaCreacion
) {
    public static SubitemResponseDTO of(
            Integer id,
            Integer idTarea,
            String description,
            Boolean checked,
            LocalDateTime fechaCreacion
    ) {
        return new SubitemResponseDTO(id, idTarea, description, checked, fechaCreacion);
    }
}
