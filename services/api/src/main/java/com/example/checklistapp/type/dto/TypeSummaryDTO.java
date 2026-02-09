package com.example.checklistapp.type.dto;

public record TypeSummaryDTO(
    Integer id,
    String nombre,
    String color
) {

    public boolean tieneColorValido() {
        return color != null && color.matches("^#[0-9A-Fa-f]{6}$");
    }
}
