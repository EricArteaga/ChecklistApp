package com.example.checklistapp.mapper;

import com.example.checklistapp.dto.task.*;
import com.example.checklistapp.dto.type.TypeSummaryDTO;
import com.example.checklistapp.dto.user.UserSummaryDTO;
import com.example.checklistapp.entities.Task;
import com.example.checklistapp.entities.Type;
import com.example.checklistapp.entities.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper entre Entity Task y DTOs usando MapStruct.
 *
 * <p>MapStruct genera la implementación en compile-time.
 * Usa reflection solo para getters/setters estándar.
 *
 * <p><b>Configuración:</b>
 * <ul>
 *   <li>{@code componentModel = "spring"} → Genera un Spring Bean que puedes inyectar con {@literal @}Autowired</li>
 *   <li>{@code nullValuePropertyMappingStrategy = IGNORE} → En update, null significa "no cambiar este campo"</li>
 * </ul>
 *
 * <p><b>Uso en Service:</b>
 * <pre>
 * {@literal @}Autowired
 * private TaskMapper taskMapper;
 *
 * // Create
 * Task task = taskMapper.toEntity(createDTO);
 * taskRepository.save(task);
 *
 * // Response
 * Task task = taskRepository.findByIdWithUsuarioAndTipo(id).orElseThrow();
 * TaskResponseDTO dto = taskMapper.toResponseDTO(task);
 *
 * // Update (PATCH)
 * Task task = taskRepository.findById(id).orElseThrow();
 * taskMapper.updateEntityFromDTO(updateDTO, task);
 * if (updateDTO.idTipo() != null) {
 *     task.setIdTipo(updateDTO.idTipo());  // Permite setear null (remover tipo)
 * }
 * taskRepository.save(task);
 * </pre>
 *
 * @author ChecklistApp Team
 * @since 1.0
 */
@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, TypeMapper.class}  // Mappers anidados
)
public interface TaskMapper {

    // === CREATE ===

    /**
     * Convierte CreateTaskDTO a Entity Task.
     * <p>El Service debe setear campos derivados (fechaCreacion, etc).
     *
     * @param dto DTO de creación
     * @return Entidad Task
     */
    @Mapping(target = "id", ignore = true)  // ID se genera en BD
    @Mapping(target = "fechaCreacion", ignore = true)  // Se setea con @PrePersist
    @Mapping(target = "usuario", ignore = true)  // Se resuelve con idUsuario
    @Mapping(target = "tipo", ignore = true)  // Se resuelve con idTipo
    Task toEntity(CreateTaskDTO dto);

    // === RESPONSE ===

    /**
     * Convierte Entity Task a TaskResponseDTO.
     * <p>Requiere que Task tenga usuario y tipo cargados (JOIN FETCH).
     *
     * @param task Entidad Task con relaciones cargadas
     * @return DTO completo
     */
    @Mapping(source = "usuario", target = "usuario")
    @Mapping(source = "tipo", target = "tipo")
    TaskResponseDTO toResponseDTO(Task task);

    // === SUMMARY ===

    /**
     * Convierte Entity Task a TaskSummaryDTO.
     * <p>Requiere que Task tenga tipo cargado (JOIN FETCH).
     *
     * @param task Entidad Task con relación tipo cargada
     * @return DTO resumido
     */
    @Mapping(source = "tipo", target = "tipo")
    @Mapping(target = "idUsuario", source = "idUsuario")  // ID, no objeto
    TaskSummaryDTO toSummaryDTO(Task task);

    /**
     * Convierte una lista de Entity Task a lista de TaskSummaryDTO.
     *
     * @param tasks Lista de entidades
     * @return Lista de DTOs resumidos
     */
    List<TaskSummaryDTO> toSummaryDTOList(List<Task> tasks);

    // === UPDATE ===

    /**
     * Actualiza entity con valores de DTO (PATCH).
     *
     * <p><b>Estrategia:</b> NULL en DTO significa "no actualizar este campo".
     *
     * <p><b>EXCEPCIÓN:</b> Para {@code idTipo}, NULL significa "remover el tipo".
     * MapStruct no puede diferenciar estos casos, así que debes manejarlo manualmente:
     * <pre>
     * taskMapper.updateEntityFromDTO(dto, task);
     * if (dto.idTipo() != null) {  // Se envió explícitamente
     *     task.setIdTipo(dto.idTipo());  // Permite setear null
     * }
     * </pre>
     *
     * @param dto DTO con campos a actualizar (null = no cambiar)
     * @param entity Entidad a actualizar
     */
    @Mapping(target = "id", ignore = true)  // ID es inmutable
    @Mapping(target = "idUsuario", ignore = true)  // No se permite cambiar propietario
    @Mapping(target = "fechaCreacion", ignore = true)  // Fecha creación es inmutable
    @Mapping(target = "usuario", ignore = true)  // Relación gestionada por idUsuario
    @Mapping(target = "tipo", ignore = true)  // Relación gestionada por idTipo
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateTaskDTO dto, @MappingTarget Task entity);
}

/**
 * Mapper auxiliar para User → UserSummaryDTO.
 * <p>MapStruct lo usará automáticamente cuando vea {@literal @}Mapping(source = "usuario", ...).
 */
@Mapper(componentModel = "spring")
interface UserMapper {
    @Mapping(target = "correo", source = "correo")  // Mapeo directo
    UserSummaryDTO toSummaryDTO(User user);
}

/**
 * Mapper auxiliar para Type → TypeSummaryDTO.
 * <p>MapStruct lo usará automáticamente cuando vea {@literal @}Mapping(source = "tipo", ...).
 */
@Mapper(componentModel = "spring")
interface TypeMapper {
    @Mapping(target = "descripcion", ignore = true)  // Type no tiene descripcion, puede ser null
    TypeSummaryDTO toSummaryDTO(Type type);
}
