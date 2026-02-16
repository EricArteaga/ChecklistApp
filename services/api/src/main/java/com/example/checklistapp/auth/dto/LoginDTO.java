package com.example.checklistapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para iniciar sesión
 */
public record LoginDTO(

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    String password

) {
}
