package com.example.checklistapp.user.dto;

public record UserSummaryDTO(
    Integer id,
    String nombre,
    String correo
) {

    public String getIniciales() {
        if (nombre == null || nombre.isEmpty()) {
            return "";
        }
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return (partes[0].charAt(0) + "" + partes[1].charAt(0)).toUpperCase();
        }
        return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
    }
}
