package com.example.checklistapp.dto.user;

/**
 * DTO resumido de User para evitar exponer información sensible.
 *
 * <p>Casos de uso:
 * <ul>
 *   <li>Anidado en TaskResponseDTO</li>
 *   <li>Anidado en otros DTOs (CommentDTO, AttachmentDTO, etc)</li>
 * </ul>
 *
 * <p><b>NO incluye:</b>
 * <ul>
 *   <li>hashContrasena (obviamente)</li>
 *   <li>roles (a menos que el usuario tenga permiso para verlos)</li>
 *   <li>telefono, direccion (PII que no siempre es necesario)</li>
 * </ul>
 *
 * <p>Requiere validación de permisos en Service:
 * <ul>
 *   <li>¿El usuario que pide la tarea tiene permiso para ver el email?</li>
 *   <li>Si no, retorna null en email o usa un DTO diferente</li>
 * </ul>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
public record UserSummaryDTO(

    Integer id,
    String nombre,
    String correo  // "correo" no "email" para consistencia con la entidad User

) {

    /**
     * Factory method para crear el DTO desde una entidad User.
     *
     * @param id ID del usuario
     * @param nombre Nombre del usuario
     * @param correo Correo electrónico del usuario
     * @return UserSummaryDTO completamente poblado
     */
    public static UserSummaryDTO of(Integer id, String nombre, String correo) {
        return new UserSummaryDTO(id, nombre, correo);
    }

    /**
     * Helper para UI: retorna iniciales del nombre.
     * <p>Ejemplo: "Juan Pérez" → "JP"
     *
     * @return iniciales en mayúsculas
     */
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "??";
        }

        var partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
        }
        return (nombre.charAt(0) + "").toUpperCase();
    }
}
