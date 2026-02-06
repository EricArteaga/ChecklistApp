/**
 * Paquete de Data Transfer Objects (DTOs).
 *
 * <p><b>Propósito:</b> Transferir datos entre capas sin exponer entidades JPA.
 *
 * <p><b>Principios:</b>
 * <ul>
 *   <li>Inmutabilidad: Usar Java Records (Java 17+)</li>
 *   <li>Validación: Anotaciones Jakarta Validation (@NotNull, @Size, etc)</li>
 *   <li>Separación: DTOs específicos por operación (Create, Update, Response)</li>
 * </ul>
 *
 * <p><b>Estructura:</b>
 * <pre>
 * dto/
 * ├── task/      → DTOs relacionados con Task
 * ├── user/      → DTOs relacionados con User
 * └── type/      → DTOs relacionados con Type
 * </pre>
 *
 * @since 1.0
 */
package com.example.checklistapp.dto;
