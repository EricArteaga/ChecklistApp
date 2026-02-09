package com.example.checklistapp.type.dto;

public record TypeResponseDTO(
    Integer id,
    Integer idUsuario,
    String nombre,
    String color,
    String descripcion
) {
    public boolean tieneColorValido() {
        return color != null && color.matches("^#[0-9A-Fa-f]{6}$");
    }
}
