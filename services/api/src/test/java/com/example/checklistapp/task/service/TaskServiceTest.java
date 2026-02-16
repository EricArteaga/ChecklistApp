package com.example.checklistapp.task.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.mapper.TaskMapper;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.type.repository.TypeRepository;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TaskService
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@ExtendWith(MockitoExtension.class)**: Habilita Mockito para JUnit 5
 * 2. **@Mock**: Crea un simulacro (mock) de dependencias externas (repositories, mappers)
 * 3. **@InjectMocks**: Crea la instancia de TaskService e inyecta los mocks automáticamente
 * 4. **when/thenReturn**: Configura el comportamiento de los mocks
 * 5. **verify**: Confirma que se llamó a un método del mock
 *
 * ESTRATEGIA DE TESTING:
 * ======================
 * - Test unitario puro: NO usamos @SpringBootTest (más rápido, sin contexto Spring)
 * - Aislamiento: Mockeamos todas las dependencias (repositories, mappers)
 * - Probamos LÓGICA DE NEGOCIO, no persistencia de datos
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // === MOCKS: Dependencias que simulamos ===
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private TaskMapper taskMapper;

    // === SYSTEM UNDER TEST (SUT): Lo que estamos probando ===
    @InjectMocks
    private TaskService taskService;

    // === FIXTURE: Datos de prueba reutilizables ===
    private User testUser;
    private Type testType;
    private Task testTask;
    private CreateTaskDTO createTaskDTO;
    private TaskResponseDTO taskResponseDTO;

    /**
     * @BeforeEach: Se ejecuta ANTES de CADA test
     * Inicializa los objetos de prueba para evitar duplicación de código
     */
    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");

        // Crear tipo de prueba
        testType = new Type();
        testType.setId(1);
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");

        // Crear tarea de prueba
        testTask = new Task();
        testTask.setId(1);
        testTask.setUsuario(testUser);
        testTask.setTipo(testType);
        testTask.setNombre("Completar reporte");
        testTask.setDescripcion("Reporte mensual");
        testTask.setCompletada(false);
        testTask.setFechaProgramacion(LocalDate.now().plusDays(7));

        // Crear SummaryDTOs para las respuestas
        com.example.checklistapp.user.dto.UserSummaryDTO userSummaryDTO =
            new com.example.checklistapp.user.dto.UserSummaryDTO(1, "test@example.com", "Usuario Test");
        com.example.checklistapp.type.dto.TypeSummaryDTO typeSummaryDTO =
            new com.example.checklistapp.type.dto.TypeSummaryDTO(1, "Trabajo", "#326cc3");

        // DTO para crear tarea (orden: nombre, descripcion, idUsuario, idTipo, fechaProgramacion, completada, fechaRealizacion)
        createTaskDTO = new CreateTaskDTO(
                "Completar reporte",
                "Reporte mensual",
                1, // idUsuario
                1, // idTipo
                LocalDate.now().plusDays(7), // fechaProgramacion
                false, // completada
                null  // fechaRealizacion
        );

        // DTO de respuesta esperado (orden: id, nombre, descripcion, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, usuario, tipo)
        taskResponseDTO = new TaskResponseDTO(
                1,
                "Completar reporte",
                "Reporte mensual",
                null, // fechaCreacion
                LocalDate.now().plusDays(7),
                null, // fechaRealizacion
                false,
                userSummaryDTO,
                typeSummaryDTO
        );
    }

    // ========================================
    // TESTS CREATE: Creación de tareas
    // ========================================

    @Test
    @DisplayName("✅ create() - Crea tarea exitosamente con usuario y tipo válidos")
    void create_WithValidData_ReturnsTaskResponseDTO() {
        // GIVEN: Configuración de los mocks (preparación del escenario)
        // Cuando busque usuario ID 1, retornar testUser
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        // Cuando busque tipo ID 1, retornar testType
        when(typeRepository.findById(1)).thenReturn(Optional.of(testType));
        // Cuando convierta DTO a Entity, retornar testTask
        when(taskMapper.toEntity(any(CreateTaskDTO.class))).thenReturn(testTask);
        // Cuando guarde la tarea, retornar la misma tarea (simula auto-generación de ID)
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        // Cuando busque tarea con relaciones, retornar testTask
        when(taskRepository.findByIdWithUsuarioAndTipo(1)).thenReturn(Optional.of(testTask));
        // Cuando convierta Entity a DTO, retornar taskResponseDTO
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(taskResponseDTO);

        // WHEN: Ejecución del método a probar
        TaskResponseDTO result = taskService.create(createTaskDTO);

        // THEN: Verificaciones (assertions + verify)
        // 1. Verificamos que el resultado no sea null
        assertThat(result).isNotNull();
        // 2. Verificamos que el ID sea el esperado
        assertThat(result.id()).isEqualTo(1);
        // 3. Verificamos que el nombre sea el correcto
        assertThat(result.nombre()).isEqualTo("Completar reporte");

        // 4. Verificamos que se llamó a userRepository.findById exactamente 1 vez
        verify(userRepository, times(1)).findById(1);
        // 5. Verificamos que se llamó a typeRepository.findById exactamente 1 vez
        verify(typeRepository, times(1)).findById(1);
        // 6. Verificamos que se llamó a taskRepository.save exactamente 1 vez
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("❌ create() - Lanza ResourceNotFoundException cuando usuario no existe")
    void create_WhenUserNotExists_ThrowsResourceNotFoundException() {
        // GIVEN: Usuario no encontrado
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        CreateTaskDTO dtoWithInvalidUser = new CreateTaskDTO(
                "Tarea",
                "Descripción",
                999, // idUsuario inexistente
                1,
                LocalDate.now(),
                false,
                null
        );

        // WHEN & THEN: Ejecutamos y esperamos excepción
        // assertThatThrownBy: AssertJ syntax para verificar excepciones
        assertThatThrownBy(() -> taskService.create(dtoWithInvalidUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("999");

        // Verificamos que NUNCA se llamó a save (porque falló antes)
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("❌ create() - Lanza ResourceNotFoundException cuando tipo no existe")
    void create_WhenTypeNotExists_ThrowsResourceNotFoundException() {
        // GIVEN: Usuario existe pero tipo no
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(typeRepository.findById(999)).thenReturn(Optional.empty());

        CreateTaskDTO dtoWithInvalidType = new CreateTaskDTO(
                "Tarea",
                "Descripción",
                1,
                999, // idTipo inexistente
                LocalDate.now(),
                false,
                null
        );

        // WHEN & THEN
        assertThatThrownBy(() -> taskService.create(dtoWithInvalidType))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tipo")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("✅ create() - Asigna fechaRealización automática cuando marca completada sin fecha")
    void create_WhenMarkedCompletedWithoutDate_SetsTodayAsCompletionDate() {
        // GIVEN: Tarea marcada como completada pero sin fecha de realización
        CreateTaskDTO completedTaskDTO = new CreateTaskDTO(
                "Tarea",
                "Descripción",
                1,
                1,
                LocalDate.now(),
                true,  // Marcada como completada
                null // Sin fecha de realización explícita
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(typeRepository.findById(1)).thenReturn(Optional.of(testType));

        // Usamos Answer para capturar y modificar la tarea que se está guardando
        when(taskMapper.toEntity(any(CreateTaskDTO.class))).thenAnswer(invocation -> {
            CreateTaskDTO dto = invocation.getArgument(0);
            Task task = new Task();
            task.setNombre(dto.nombre());
            task.setDescripcion(dto.descripcion());
            task.setCompletada(dto.completada());
            task.setFechaProgramacion(dto.fechaProgramacion());
            return task;
        });

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            // Simula auto-generación de ID
            task.setId(1);
            return task;
        });

        when(taskRepository.findByIdWithUsuarioAndTipo(1)).thenAnswer(invocation -> {
            // Retorna la tarea que acaba de ser guardada
            Task task = new Task();
            task.setId(1);
            task.setNombre("Tarea");
            return Optional.of(task);
        });

        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(taskResponseDTO);

        // WHEN
        taskService.create(completedTaskDTO);

        // THEN: Verificamos que se llamó a save con una tarea que tiene fechaRealizacion = hoy
        verify(taskRepository, times(1)).save(argThat(task ->
                task.getFechaRealizacion() != null &&
                task.getFechaRealizacion().equals(LocalDate.now())
        ));
    }

    @Test
    @DisplayName("❌ create() - Lanza BusinessRuleException cuando fechaRealización < fechaProgramación")
    void create_WhenCompletionDateBeforeScheduledDate_ThrowsBusinessRuleException() {
        // GIVEN: Fecha de realización ANTES de fecha de programación (inválido)
        CreateTaskDTO invalidDatesDTO = new CreateTaskDTO(
                "Tarea",
                "Descripción",
                1,
                1,
                LocalDate.now().plusDays(7),  // Programada para en 7 días
                false,
                LocalDate.now()               // Completada hoy (¡inválido!)
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(typeRepository.findById(1)).thenReturn(Optional.of(testType));

        // Usamos Answer para crear una tarea con las fechas inválidas
        when(taskMapper.toEntity(any(CreateTaskDTO.class))).thenAnswer(invocation -> {
            CreateTaskDTO dto = invocation.getArgument(0);
            Task task = new Task();
            task.setNombre(dto.nombre());
            task.setDescripcion(dto.descripcion());
            task.setCompletada(dto.completada());
            task.setFechaProgramacion(dto.fechaProgramacion());
            task.setFechaRealizacion(dto.fechaRealizacion());
            return task;
        });

        // No necesitamos mockear save ni findByIdWithUsuarioAndTipo porque se lanza excepción antes

        // WHEN & THEN
        assertThatThrownBy(() -> taskService.create(invalidDatesDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fecha de realización")
                .hasMessageContaining("anterior");
    }

    // ========================================
    // TESTS FIND: Consultas de tareas
    // ========================================

    @Test
    @DisplayName("✅ findById() - Retorna tarea cuando existe")
    void findById_WhenTaskExists_ReturnsTaskResponseDTO() {
        // GIVEN
        when(taskRepository.findByIdWithUsuarioAndTipo(1)).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(taskResponseDTO);

        // WHEN
        TaskResponseDTO result = taskService.findById(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        verify(taskRepository, times(1)).findByIdWithUsuarioAndTipo(1);
    }

    @Test
    @DisplayName("❌ findById() - Lanza ResourceNotFoundException cuando tarea no existe")
    void findById_WhenTaskNotExists_ThrowsResourceNotFoundException() {
        // GIVEN
        when(taskRepository.findByIdWithUsuarioAndTipo(999)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> taskService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tarea")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("✅ findByIdUsuario() - Retorna lista de tareas del usuario")
    void findByIdUsuario_WhenUserExists_ReturnsTaskList() {
        // GIVEN
        com.example.checklistapp.type.dto.TypeSummaryDTO typeSummaryDTO =
            new com.example.checklistapp.type.dto.TypeSummaryDTO(1, "Trabajo", "#326cc3");

        // TaskSummaryDTO: id, nombre, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, idUsuario, tipo
        TaskSummaryDTO summaryDTO = new TaskSummaryDTO(
                1,
                "Tarea",
                null, // fechaCreacion
                LocalDate.now().plusDays(7),
                null, // fechaRealizacion
                false,
                1, // idUsuario
                typeSummaryDTO
        );
        List<Task> tasks = List.of(testTask);
        List<TaskSummaryDTO> expectedDTOs = List.of(summaryDTO);

        when(userRepository.existsById(1)).thenReturn(true);
        when(taskRepository.findByIdUsuarioWithUsuarioAndTipo(1)).thenReturn(tasks);
        when(taskMapper.toSummaryDTOList(tasks)).thenReturn(expectedDTOs);

        // WHEN
        List<TaskSummaryDTO> result = taskService.findByIdUsuario(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Tarea");
        verify(userRepository, times(1)).existsById(1);
    }

    @Test
    @DisplayName("❌ findByIdUsuario() - Lanza excepción cuando usuario no existe")
    void findByIdUsuario_WhenUserNotExists_ThrowsResourceNotFoundException() {
        // GIVEN
        when(userRepository.existsById(999)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> taskService.findByIdUsuario(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================================
    // TESTS UPDATE: Actualización de tareas
    // ========================================

    @Test
    @DisplayName("✅ update() - Actualiza tarea exitosamente")
    void update_WithValidData_UpdatesTask() {
        // GIVEN
        // UpdateTaskDTO: nombre, descripcion, completada, fechaProgramacion, fechaRealizacion, idTipo
        UpdateTaskDTO updateDTO = new UpdateTaskDTO(
                "Nombre actualizado",
                null, // descripcion no cambia (null = IGNORE con MapStruct)
                true, // Marcar como completada
                null, // fechaProgramacion no cambia
                LocalDate.now(), // Nueva fecha de realización
                null // idTipo no cambia
        );

        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        // Solo mockeamos typeRepository si idTipo no es null (en este caso es null)
        // when(typeRepository.findById(1)).thenReturn(Optional.of(testType));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskRepository.findByIdWithUsuarioAndTipo(1)).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(taskResponseDTO);

        // WHEN
        TaskResponseDTO result = taskService.update(1, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // ========================================
    // TESTS DELETE: Eliminación de tareas
    // ========================================

    @Test
    @DisplayName("✅ delete() - Elimina tarea cuando existe")
    void delete_WhenTaskExists_DeletesTask() {
        // GIVEN
        when(taskRepository.existsById(1)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1);

        // WHEN
        taskService.delete(1);

        // THEN
        verify(taskRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("❌ delete() - Lanza excepción cuando tarea no existe")
    void delete_WhenTaskNotExists_ThrowsResourceNotFoundException() {
        // GIVEN
        when(taskRepository.existsById(999)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> taskService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).deleteById(anyInt());
    }

    /*
     * RESUMEN DE PATRONES DE TESTING USADOS:
     * ======================================
     * 1. GIVEN-WHEN-THEN: Estructura clara de preparación-ejecución-verificación
     * 2. Descriptivo: @DisplayName con emojis para identificar éxito/fracaso esperado
     * 3. Aislamiento: Cada test es independiente gracias a @BeforeEach y mocks
     * 4. Verificación doble: assertThat (resultado) + verify (interacciones con mocks)
     * 5. Cobertura de casos: Happy path ✅ + Edge cases ❌
     */
}
