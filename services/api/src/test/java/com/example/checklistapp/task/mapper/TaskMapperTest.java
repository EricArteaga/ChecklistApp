package com.example.checklistapp.task.mapper;

import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskMapper (MapStruct)
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **Mappers.getMapper()**: Crea instancia del mapper generada por MapStruct
 * 2. **Pruebas de mapeo**: Verifica que Entity ↔ DTO se mapean correctamente
 * 3. **@Mapping annotations**: Verifica configuración de mapeos especiales
 *
 * QUÉ PROBAMOS:
 * =============
 * - CreateTaskDTO → Task (Entity)
 * - Task → TaskResponseDTO (con relaciones anidadas)
 * - Task → TaskSummaryDTO (resumen)
 * - List<Task> → List<TaskSummaryDTO>
 * - UpdateTaskDTO → Task (actualización parcial con nullValueIgnore)
 */
@DisplayName("TaskMapper Integration Tests")
class TaskMapperTest {

    private TaskMapper taskMapper;
    private User testUser;
    private Type testType;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Usamos Mappers.getMapper() para crear instancia sin Spring
        taskMapper = Mappers.getMapper(TaskMapper.class);

        // Crear usuario de prueba
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");
        testUser.setFechaCreacion(LocalDateTime.now());

        // Crear tipo de prueba
        testType = new Type();
        testType.setId(1);
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");
        testType.setIdUsuario(1);

        // Crear tarea de prueba con relaciones
        testTask = new Task();
        testTask.setId(1);
        testTask.setNombre("Tarea de prueba");
        testTask.setDescripcion("Descripción de prueba");
        testTask.setIdUsuario(1);
        testTask.setIdTipo(1);
        testTask.setUsuario(testUser);
        testTask.setTipo(testType);
        testTask.setCompletada(false);
        testTask.setFechaCreacion(LocalDateTime.now());
        testTask.setFechaProgramacion(LocalDate.now().plusDays(7));
        testTask.setSubitems(List.of());
    }

    // ========================================
    // TESTS TO ENTITY (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ toEntity - CreateTaskDTO to Task (ignores id, fechaCreacion, relations)")
    void toEntity_CreateTaskDTO_ReturnsTaskWithCorrectFields() {
        // GIVEN: CreateTaskDTO
        CreateTaskDTO dto = new CreateTaskDTO(
                "Nueva tarea",
                "Descripción",
                1, // idUsuario
                1, // idTipo
                LocalDate.now().plusDays(5),
                false,
                null
        );

        // WHEN: Mapear a Entity
        Task entity = taskMapper.toEntity(dto);

        // THEN: Campos mapeados correctamente
        assertThat(entity.getNombre()).isEqualTo("Nueva tarea");
        assertThat(entity.getDescripcion()).isEqualTo("Descripción");
        assertThat(entity.getIdUsuario()).isEqualTo(1);
        assertThat(entity.getIdTipo()).isEqualTo(1);
        assertThat(entity.getFechaProgramacion()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(entity.getCompletada()).isFalse();

        // Campos ignorados por @Mapping(target = ..., ignore = true)
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFechaCreacion()).isNull();
        assertThat(entity.getUsuario()).isNull();
        assertThat(entity.getTipo()).isNull();
        assertThat(entity.getSubitems()).isNull();
    }

    @Test
    @DisplayName("✅ toEntity - Task with completion date")
    void toEntity_TaskWithCompletionDate_MapsAllFields() {
        // GIVEN: CreateTaskDTO con fecha de realización
        LocalDate completionDate = LocalDate.now();
        CreateTaskDTO dto = new CreateTaskDTO(
                "Tarea completada",
                "Descripción",
                1,
                1,
                LocalDate.now().plusDays(3),
                true,
                completionDate
        );

        // WHEN: Mapear a Entity
        Task entity = taskMapper.toEntity(dto);

        // THEN: Fecha de realización mapeada
        assertThat(entity.getFechaRealizacion()).isEqualTo(completionDate);
        assertThat(entity.getCompletada()).isTrue();
    }

    // ========================================
    // TESTS TO RESPONSE DTO
    // ========================================

    @Test
    @DisplayName("✅ toResponseDTO - Task to TaskResponseDTO with nested objects")
    void toResponseDTO_TaskWithRelations_ReturnsDTOWithNestedObjects() {
        // WHEN: Mapear a ResponseDTO
        TaskResponseDTO dto = taskMapper.toResponseDTO(testTask);

        // THEN: Campos básicos mapeados
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nombre()).isEqualTo("Tarea de prueba");
        assertThat(dto.descripcion()).isEqualTo("Descripción de prueba");
        assertThat(dto.completada()).isFalse();
        assertThat(dto.fechaCreacion()).isNotNull();
        assertThat(dto.fechaProgramacion()).isEqualTo(LocalDate.now().plusDays(7));

        // Objetos anidados (usuario, tipo, subitems)
        assertThat(dto.usuario()).isNotNull();
        assertThat(dto.usuario().correo()).isEqualTo("test@example.com");
        assertThat(dto.usuario().nombre()).isEqualTo("Usuario Test");

        assertThat(dto.tipo()).isNotNull();
        assertThat(dto.tipo().nombre()).isEqualTo("Trabajo");
        assertThat(dto.tipo().color()).isEqualTo("#326cc3");

        assertThat(dto.subitems()).isNotNull();
        assertThat(dto.subitems()).isEmpty();
    }

    @Test
    @DisplayName("✅ toResponseDTO - Task with subitems")
    void toResponseDTO_TaskWithSubitems_ReturnsDTOWithSubitemsList() {
        // GIVEN: Tarea con subitems
        Subitem subitem1 = new Subitem();
        subitem1.setId(1);
        subitem1.setDescription("Subitem 1");
        subitem1.setChecked(false);
        subitem1.setFechaCreacion(LocalDateTime.now());

        Subitem subitem2 = new Subitem();
        subitem2.setId(2);
        subitem2.setDescription("Subitem 2");
        subitem2.setChecked(true);
        subitem2.setFechaCreacion(LocalDateTime.now());

        testTask.setSubitems(List.of(subitem1, subitem2));

        // WHEN: Mapear a ResponseDTO
        TaskResponseDTO dto = taskMapper.toResponseDTO(testTask);

        // THEN: Subitems mapeados
        assertThat(dto.subitems()).hasSize(2);
        assertThat(dto.subitems().get(0).description()).isEqualTo("Subitem 1");
        assertThat(dto.subitems().get(1).description()).isEqualTo("Subitem 2");
    }

    @Test
    @DisplayName("✅ toResponseDTO - Task with null usuario (anonymous task)")
    void toResponseDTO_TaskWithNullUsuario_ReturnsDTOWithNullUsuario() {
        // GIVEN: Tarea sin usuario (tarea anónima)
        testTask.setUsuario(null);
        testTask.setIdUsuario(null);

        // WHEN: Mapear a ResponseDTO
        TaskResponseDTO dto = taskMapper.toResponseDTO(testTask);

        // THEN: usuario es null (tarea anónima)
        assertThat(dto.usuario()).isNull();
    }

    // ========================================
    // TESTS TO SUMMARY DTO
    // ========================================

    @Test
    @DisplayName("✅ toSummaryDTO - Task to TaskSummaryDTO")
    void toSummaryDTO_Task_ReturnsDTOWithEssentialFields() {
        // WHEN: Mapear a SummaryDTO
        TaskSummaryDTO dto = taskMapper.toSummaryDTO(testTask);

        // THEN: Campos esenciales mapeados
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nombre()).isEqualTo("Tarea de prueba");
        assertThat(dto.fechaCreacion()).isNotNull();
        assertThat(dto.fechaProgramacion()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(dto.completada()).isFalse();
        assertThat(dto.idUsuario()).isEqualTo(1);

        // Tipo anidado
        assertThat(dto.tipo()).isNotNull();
        assertThat(dto.tipo().nombre()).isEqualTo("Trabajo");
    }

    @Test
    @DisplayName("✅ toSummaryDTOList - List of tasks to list of DTOs")
    void toSummaryDTOList_ListOfTasks_ReturnsListOfDTOs() {
        // GIVEN: Lista de tareas
        Task task1 = testTask;
        Task task2 = new Task();
        task2.setId(2);
        task2.setNombre("Tarea 2");
        task2.setIdUsuario(1);
        task2.setIdTipo(1);
        task2.setTipo(testType);
        task2.setCompletada(true);
        task2.setFechaProgramacion(LocalDate.now().plusDays(3));

        List<Task> tasks = List.of(task1, task2);

        // WHEN: Mapear lista
        List<TaskSummaryDTO> dtos = taskMapper.toSummaryDTOList(tasks);

        // THEN: Lista de DTOs
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).id()).isEqualTo(1);
        assertThat(dtos.get(0).nombre()).isEqualTo("Tarea de prueba");
        assertThat(dtos.get(1).id()).isEqualTo(2);
        assertThat(dtos.get(1).nombre()).isEqualTo("Tarea 2");
    }

    @Test
    @DisplayName("✅ toSummaryDTOList - Empty list returns empty list")
    void toSummaryDTOList_EmptyList_ReturnsEmptyList() {
        // WHEN: Mapear lista vacía
        List<TaskSummaryDTO> dtos = taskMapper.toSummaryDTOList(List.of());

        // THEN: Lista vacía
        assertThat(dtos).isEmpty();
    }

    // ========================================
    // TESTS UPDATE ENTITY FROM DTO
    // ========================================

    @Test
    @DisplayName("✅ updateEntityFromDTO - Partial update (null fields ignored)")
    void updateEntityFromDTO_PartialUpdate_OnlyUpdatesNonNullFields() {
        // GIVEN: UpdateTaskDTO con solo algunos campos (parcial)
        UpdateTaskDTO dto = new UpdateTaskDTO(
                "Nombre actualizado",  // nuevo nombre
                null,                   // descripcion null = no cambiar
                true,                   // marcar como completada
                null,                   // fechaProgramacion null = no cambiar
                LocalDate.now(),       // nueva fecha de realización
                null                    // idTipo null = no cambiar
        );

        Task entity = new Task();
        entity.setNombre("Nombre original");
        entity.setDescripcion("Descripción original");
        entity.setCompletada(false);
        entity.setFechaProgramacion(LocalDate.now().plusDays(7));

        // WHEN: Actualizar entidad
        taskMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo campos no-null se actualizaron
        assertThat(entity.getNombre()).isEqualTo("Nombre actualizado");  // Cambió
        assertThat(entity.getDescripcion()).isEqualTo("Descripción original"); // No cambió (null en DTO)
        assertThat(entity.getCompletada()).isTrue();  // Cambió
        assertThat(entity.getFechaProgramacion()).isEqualTo(LocalDate.now().plusDays(7)); // No cambió (null en DTO)
        assertThat(entity.getFechaRealizacion()).isEqualTo(LocalDate.now()); // Cambió
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Full update")
    void updateEntityFromDTO_FullUpdate_UpdatesAllFields() {
        // GIVEN: UpdateTaskDTO con todos los campos
        UpdateTaskDTO dto = new UpdateTaskDTO(
                "Nuevo nombre",
                "Nueva descripción",
                true,
                LocalDate.now().plusDays(10),
                LocalDate.now(),
                2
        );

        Task entity = new Task();
        entity.setNombre("Original");
        entity.setDescripcion("Original");

        // WHEN: Actualizar entidad
        taskMapper.updateEntityFromDTO(dto, entity);

        // THEN: Todos los campos actualizados
        assertThat(entity.getNombre()).isEqualTo("Nuevo nombre");
        assertThat(entity.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(entity.getCompletada()).isTrue();
        assertThat(entity.getFechaProgramacion()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(entity.getFechaRealizacion()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Ignored fields (id, fechaCreacion, relations)")
    void updateEntityFromDTO_IgnoredFields_DoesNotUpdateIgnoredFields() {
        // GIVEN: UpdateTaskDTO (todos los campos)
        UpdateTaskDTO dto = new UpdateTaskDTO(
                "Nombre",
                "Descripción",
                false,
                LocalDate.now(),
                null,
                1
        );

        Task entity = new Task();
        entity.setId(999);
        entity.setIdUsuario(100);
        entity.setFechaCreacion(LocalDateTime.now().minusDays(10));

        // WHEN: Actualizar entidad
        taskMapper.updateEntityFromDTO(dto, entity);

        // THEN: Campos ignorados no cambian (@Mapping(target = ..., ignore = true))
        assertThat(entity.getId()).isEqualTo(999); // Ignorado
        assertThat(entity.getIdUsuario()).isEqualTo(100); // Ignorado
        assertThat(entity.getFechaCreacion()).isNotNull(); // Ignorado
    }
}
