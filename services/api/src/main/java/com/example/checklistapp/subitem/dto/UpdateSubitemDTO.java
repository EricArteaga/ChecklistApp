package com.example.checklistapp.subitem.dto;

import jakarta.validation.constraints.Size;

public record UpdateSubitemDTO(
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String description,

        Boolean checked
) {
    public boolean tieneCambios() {
        return description != null || checked != null;
    }
}
