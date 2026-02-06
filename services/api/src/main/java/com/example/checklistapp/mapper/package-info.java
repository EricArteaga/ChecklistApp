/**
 * Paquete de Mappers MapStruct.
 *
 * <p><b>Propósito:</b> Convertir entre Entidades JPA y DTOs de forma eficiente.
 *
 * <p><b>Ventajas de MapStruct:</b>
 * <ul>
 *   <li>Genera código en compile-time (no reflection overhead)</li>
 *   <li>Type-safe (errores de compilación en lugar de runtime)</li>
 *   <li>Integración con Spring (componentModel = "spring")</li>
 *   <li>Soporta mapeos complejos (@Mapping, @BeanMapping)</li>
 * </ul>
 *
 * <p><b>Uso típico:</b>
 * <pre>
 * {@literal @}Autowired
 * private TaskMapper taskMapper;
 *
 * // Entity → DTO
 * TaskResponseDTO dto = taskMapper.toResponseDTO(task);
 *
 * // DTO → Entity
 * Task task = taskMapper.toEntity(createDTO);
 *
 * // Update (PATCH)
 * taskMapper.updateEntityFromDTO(updateDTO, task);
 * </pre>
 *
 * @since 1.0
 */
package com.example.checklistapp.mapper;
