package com.example.checklistapp.type.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTypeDTO(
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String nombre,

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un código hexadecimal válido (ej: #326cc3)")
    String color,

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    String descripcion
) {
    public boolean tieneCambios() {
        return nombre != null || color != null || descripcion != null;
    }
}
