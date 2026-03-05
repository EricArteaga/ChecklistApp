package com.example.checklistapp.subitem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubitemDTO(
        @NotBlank(message = "La descripción es requerida")
        @Size(min = 1, max = 500, message = "La descripción debe tener entre 1 y 500 caracteres")
        String description,

        Boolean checked
) {
    public CreateSubitemDTO {
        if (checked == null) {
            checked = false;
        }
    }
}
