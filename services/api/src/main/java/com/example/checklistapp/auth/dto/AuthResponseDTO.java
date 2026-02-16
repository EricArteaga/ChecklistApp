package com.example.checklistapp.auth.dto;

import com.example.checklistapp.user.dto.UserResponseDTO;

/**
 * DTO de respuesta después de login/registro
 */
public record AuthResponseDTO(
    String token,           // JWT Access Token
    String refreshToken,    // Refresh Token (opcional, para renovación)
    UserResponseDTO user,   // Datos del usuario autenticado
    String type             // "Bearer" (tipo de token)
) {
}
