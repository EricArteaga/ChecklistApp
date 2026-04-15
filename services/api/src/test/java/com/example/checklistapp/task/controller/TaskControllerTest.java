package com.example.checklistapp.task.controller;

import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.service.TaskService;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@WebMvcTest**: Configura solo el contexto web para probar controllers
 *    - Carga MockMvc, el controller especificado, y componentes MVC
 *    - NO carga servicios, repos, ni el resto de la aplicación
 * 2. **@MockBean**: Crea un mock del servicio para aislar el controller
 * 3. **MockMvc**: Permite simular peticiones HTTP y verificar respuestas
 *
 * QUÉ PROBAMOS:
 * =============
 * - Status codes HTTP (200, 201, 204, 400, 404)
 * - Estructura JSON de respuestas
 * - Validaciones de entrada (@Valid, @RequestBody)
 * - Routing de endpoints
 * - Content-Type y headers
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    // === FIXTURE: DTOs de prueba ===
    private TaskResponseDTO taskResponseDTO;
    private TaskSummaryDTO taskSummaryDTO;
    private CreateTaskDTO createTaskDTO;
    private UpdateTaskDTO updateTaskDTO;

    @BeforeEach
    void setUp() {
        // Crear TypeSummaryDTO
        TypeSummaryDTO typeSummaryDTO = new TypeSummaryDTO(1, "Trabajo", "#326cc3");

        // TaskResponseDTO: id, nombre, descripcion, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, usuario, tipo, subitems
        taskResponseDTO = new TaskResponseDTO(
                1,
                "Tarea de prueba",
                "Descripción de prueba",
                null, // fechaCreacion
                LocalDate.now().plusDays(7),
                null, // fechaRealizacion
                false,
                null, // usuario (opcional)
                typeSummaryDTO,
                Collections.emptyList() // subitems
        );

        // TaskSummaryDTO: id, nombre, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, idUsuario, tipo
        taskSummaryDTO = new TaskSummaryDTO(
                1,
                "Tarea resumida",
                null, // fechaCreacion
                LocalDate.now().plusDays(5),
                null, // fechaRealizacion
                false,
                1, // idUsuario
                typeSummaryDTO
        );

        // CreateTaskDTO: nombre, descripcion, idUsuario, idTipo, fechaProgramacion, completada, fechaRealizacion
        createTaskDTO = new CreateTaskDTO(
                "Nueva tarea",
                "Descripción",
                1, // idUsuario
                1, // idTipo
                LocalDate.now().plusDays(3),
                false,
                null // fechaRealizacion
        );

        // UpdateTaskDTO: nombre, descripcion, completada, fechaProgramacion, fechaRealizacion, idTipo
        updateTaskDTO = new UpdateTaskDTO(
                "Tarea actualizada",
                null, // descripcion no cambia
                true, // marcar como completada
                null, // fechaProgramacion no cambia
                LocalDate.now(), // nueva fecha de realización
                null // idTipo no cambia
        );
    }

    // ========================================
    // TESTS CREATE
    // ========================================

    @Test
    @DisplayName("✅ POST /api/tasks - Valid task returns 201 with task")
    void create_ValidTask_Returns201WithTask() throws Exception {
        // GIVEN: Service retorna tarea creada
        when(taskService.create(any(CreateTaskDTO.class))).thenReturn(taskResponseDTO);

        // WHEN: POST tarea válida
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDTO)))

        // THEN: HTTP 201 + JSON con tarea
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tarea de prueba"))
                .andExpect(jsonPath("$.descripcion").value("Descripción de prueba"))
                .andExpect(jsonPath("$.completada").value(false))
                .andExpect(jsonPath("$.tipo.nombre").value("Trabajo"));

        verify(taskService, times(1)).create(any(CreateTaskDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tasks - Invalid DTO returns 400")
    void create_InvalidDTO_Returns400() throws Exception {
        // GIVEN: DTO con nombre vacío (violación @NotBlank)
        String invalidJson = "{\"nombre\":\"\",\"descripcion\":\"Test\",\"idUsuario\":1,\"idTipo\":1}";

        // WHEN: POST tarea inválida
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(taskService, never()).create(any(CreateTaskDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tasks - Missing required fields returns 400")
    void create_MissingRequiredFields_Returns400() throws Exception {
        // GIVEN: JSON incompleto
        String incompleteJson = "{\"nombre\":\"Tarea\"}";

        // WHEN: POST incompleto
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(taskService, never()).create(any(CreateTaskDTO.class));
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/{id} - Existing task returns 200 with task")
    void findById_ExistingTask_Returns200WithTask() throws Exception {
        // GIVEN: Service retorna tarea
        when(taskService.findById(1)).thenReturn(taskResponseDTO);

        // WHEN: GET tarea por ID
        mockMvc.perform(get("/api/tasks/1"))

        // THEN: HTTP 200 + JSON con tarea
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tarea de prueba"))
                .andExpect(jsonPath("$.completada").value(false));

        verify(taskService, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ GET /api/tasks/{id} - Non-existent task returns 404")
    void findById_NonExistentTask_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(taskService.findById(999))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Tarea", "999"));

        // WHEN: GET tarea inexistente
        mockMvc.perform(get("/api/tasks/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).findById(999);
    }

    // ========================================
    // TESTS FIND ALL
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks - Returns 200 with task list")
    void findAll_TasksExist_Returns200WithList() throws Exception {
        // GIVEN: Service retorna lista
        when(taskService.findAll()).thenReturn(List.of(taskSummaryDTO));

        // WHEN: GET todas las tareas
        mockMvc.perform(get("/api/tasks"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Tarea resumida"));

        verify(taskService, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ GET /api/tasks - Empty list returns 200 with empty array")
    void findAll_NoTasks_Returns200WithEmptyArray() throws Exception {
        // GIVEN: Service retorna lista vacía
        when(taskService.findAll()).thenReturn(Collections.emptyList());

        // WHEN: GET tareas (no hay ninguna)
        mockMvc.perform(get("/api/tasks"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS FIND BY USUARIO
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/usuario/{id} - Returns user tasks")
    void findByIdUsuario_ExistingUser_Returns200WithTasks() throws Exception {
        // GIVEN: Service retorna tareas del usuario
        when(taskService.findByIdUsuario(1)).thenReturn(List.of(taskSummaryDTO));

        // WHEN: GET tareas por usuario
        mockMvc.perform(get("/api/tasks/usuario/1"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idUsuario").value(1));

        verify(taskService, times(1)).findByIdUsuario(1);
    }

    // ========================================
    // TESTS FIND PENDING TASKS
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/usuario/{id}/pendientes - Returns pending tasks")
    void findPendingTasks_ExistingUser_Returns200WithPendingTasks() throws Exception {
        // GIVEN: Service retorna tareas pendientes
        when(taskService.findPendingTasks(1)).thenReturn(List.of(taskSummaryDTO));

        // WHEN: GET tareas pendientes
        mockMvc.perform(get("/api/tasks/usuario/1/pendientes"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        verify(taskService, times(1)).findPendingTasks(1);
    }

    // ========================================
    // TESTS FIND COMPLETED TASKS
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/usuario/{id}/completadas - Returns completed tasks")
    void findCompletedTasks_ExistingUser_Returns200WithCompletedTasks() throws Exception {
        // GIVEN: Service retorna tareas completadas
        when(taskService.findCompletedTasks(1)).thenReturn(Collections.emptyList());

        // WHEN: GET tareas completadas
        mockMvc.perform(get("/api/tasks/usuario/1/completadas"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ PATCH /api/tasks/{id} - Valid update returns 200")
    void update_ValidUpdate_Returns200() throws Exception {
        // GIVEN: Service retorna tarea actualizada
        TaskResponseDTO updatedDTO = new TaskResponseDTO(
                1,
                "Tarea actualizada",
                "Descripción",
                null,
                LocalDate.now().plusDays(3),
                LocalDate.now(),
                true,
                null,
                new TypeSummaryDTO(1, "Trabajo", "#326cc3"),
                Collections.emptyList()
        );
        when(taskService.update(eq(1), any(UpdateTaskDTO.class))).thenReturn(updatedDTO);

        // WHEN: PATCH actualización
        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTaskDTO)))

        // THEN: HTTP 200 + tarea actualizada
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tarea actualizada"))
                .andExpect(jsonPath("$.completada").value(true));

        verify(taskService, times(1)).update(eq(1), any(UpdateTaskDTO.class));
    }

    @Test
    @DisplayName("❌ PATCH /api/tasks/{id} - Invalid data returns 400")
    void update_InvalidData_Returns400() throws Exception {
        // GIVEN: DTO inválido (vacío)
        String invalidJson = "{}";

        // WHEN: PATCH con datos inválidos
        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(taskService, never()).update(anyInt(), any(UpdateTaskDTO.class));
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ DELETE /api/tasks/{id} - Returns 204 no content")
    void delete_ExistingTask_Returns204() throws Exception {
        // GIVEN: Service elimina sin lanzar excepción
        doNothing().when(taskService).delete(1);

        // WHEN: DELETE tarea
        mockMvc.perform(delete("/api/tasks/1"))

        // THEN: HTTP 204 (sin contenido)
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).delete(1);
    }

    @Test
    @DisplayName("❌ DELETE /api/tasks/{id} - Non-existent task returns 404")
    void delete_NonExistentTask_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        doThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Tarea", "999"))
                .when(taskService).delete(999);

        // WHEN: DELETE tarea inexistente
        mockMvc.perform(delete("/api/tasks/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).delete(999);
    }

    // ========================================
    // TESTS CONTENT TYPE
    // ========================================

    @Test
    @DisplayName("❌ POST /api/tasks - Wrong content type returns 415")
    void create_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/tasks - Invalid JSON returns 400")
    void create_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }
}
