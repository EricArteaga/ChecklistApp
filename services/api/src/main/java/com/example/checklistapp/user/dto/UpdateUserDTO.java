package com.example.checklistapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(
    @Email(message = "El correo debe ser válido")
    String correo,

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre
) {
    public boolean tieneCambios() {
        return correo != null || nombre != null;
    }
}
