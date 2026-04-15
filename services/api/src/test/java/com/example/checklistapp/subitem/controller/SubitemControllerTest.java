package com.example.checklistapp.subitem.controller;

import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.service.SubitemService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SubitemController
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@WebMvcTest**: Prueba solo el controller con contexto web
 * 2. **@MockBean**: Mock del servicio para aislar el controller
 * 3. **MockMvc**: Simula peticiones HTTP y verifica respuestas
 * 4. **Path variables**: idTarea viene de la URL (/api/tasks/{idTarea}/subitems)
 *
 * QUÉ PROBAMOS:
 * =============
 * - Status codes HTTP
 * - Estructura JSON de respuestas
 * - Validaciones de entrada
 * - Path variables en la URL
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SubitemController.class)
@DisplayName("SubitemController Integration Tests")
class SubitemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubitemService subitemService;

    // === FIXTURE: DTOs de prueba ===
    private SubitemResponseDTO subitemResponseDTO;
    private CreateSubitemDTO createSubitemDTO;
    private UpdateSubitemDTO updateSubitemDTO;

    @BeforeEach
    void setUp() {
        // SubitemResponseDTO: id, idTarea, description, checked, fechaCreacion
        subitemResponseDTO = new SubitemResponseDTO(
                1,
                1, // idTarea
                "Subitem de prueba",
                false,
                LocalDateTime.now()
        );

        // CreateSubitemDTO: description, checked
        createSubitemDTO = new CreateSubitemDTO(
                "Nuevo subitem",
                false
        );

        // UpdateSubitemDTO: description, checked (ambos opcionales)
        updateSubitemDTO = new UpdateSubitemDTO(
                "Subitem actualizado",
                true
        );
    }

    // ========================================
    // TESTS CREATE
    // ========================================

    @Test
    @DisplayName("✅ POST /api/tasks/{idTarea}/subitems - Valid subitem returns 201")
    void create_ValidSubitem_Returns201() throws Exception {
        // GIVEN: Service retorna subitem creado
        when(subitemService.create(eq(1), any(CreateSubitemDTO.class))).thenReturn(subitemResponseDTO);

        // WHEN: POST subitem válido
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubitemDTO)))

        // THEN: HTTP 201 + JSON con subitem
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Subitem de prueba"))
                .andExpect(jsonPath("$.checked").value(false))
                .andExpect(jsonPath("$.idTarea").value(1));

        verify(subitemService, times(1)).create(eq(1), any(CreateSubitemDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tasks/{idTarea}/subitems - Empty description returns 400")
    void create_EmptyDescription_Returns400() throws Exception {
        // GIVEN: DTO con descripción vacía
        String invalidJson = "{\"description\":\"\",\"checked\":false}";

        // WHEN: POST con descripción vacía
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(subitemService, never()).create(anyInt(), any(CreateSubitemDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tasks/{idTarea}/subitems - Missing description returns 400")
    void create_MissingDescription_Returns400() throws Exception {
        // GIVEN: JSON sin descripción
        String incompleteJson = "{\"checked\":true}";

        // WHEN: POST sin descripción
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(subitemService, never()).create(anyInt(), any(CreateSubitemDTO.class));
    }

    @Test
    @DisplayName("✅ POST /api/tasks/{idTarea}/subitems - Non-existent task returns 404")
    void create_NonExistentTask_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(subitemService.create(eq(999), any(CreateSubitemDTO.class)))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Tarea", "999"));

        // WHEN: POST a tarea inexistente
        mockMvc.perform(post("/api/tasks/999/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubitemDTO)))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(subitemService, times(1)).create(eq(999), any(CreateSubitemDTO.class));
    }

    // ========================================
    // TESTS FIND BY TAREA
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/{idTarea}/subitems - Returns 200 with subitems list")
    void findByIdTarea_SubitemsExist_Returns200WithList() throws Exception {
        // GIVEN: Service retorna lista de subitems
        when(subitemService.findByIdTarea(1)).thenReturn(List.of(subitemResponseDTO));

        // WHEN: GET subitems de tarea
        mockMvc.perform(get("/api/tasks/1/subitems"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Subitem de prueba"))
                .andExpect(jsonPath("$[0].checked").value(false));

        verify(subitemService, times(1)).findByIdTarea(1);
    }

    @Test
    @DisplayName("✅ GET /api/tasks/{idTarea}/subitems - Empty list returns 200 with empty array")
    void findByIdTarea_NoSubitems_Returns200WithEmptyArray() throws Exception {
        // GIVEN: Service retorna lista vacía
        when(subitemService.findByIdTarea(1)).thenReturn(Collections.emptyList());

        // WHEN: GET subitems (no hay ninguno)
        mockMvc.perform(get("/api/tasks/1/subitems"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tasks/{idTarea}/subitems/{id} - Existing subitem returns 200")
    void findById_ExistingSubitem_Returns200() throws Exception {
        // GIVEN: Service retorna subitem
        when(subitemService.findById(1)).thenReturn(subitemResponseDTO);

        // WHEN: GET subitem por ID
        mockMvc.perform(get("/api/tasks/1/subitems/1"))

        // THEN: HTTP 200 + JSON con subitem
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Subitem de prueba"));

        verify(subitemService, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ GET /api/tasks/{idTarea}/subitems/{id} - Non-existent subitem returns 404")
    void findById_NonExistentSubitem_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(subitemService.findById(999))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Subitem", "999"));

        // WHEN: GET subitem inexistente
        mockMvc.perform(get("/api/tasks/1/subitems/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(subitemService, times(1)).findById(999);
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ PATCH /api/tasks/{idTarea}/subitems/{id} - Valid update returns 200")
    void update_ValidUpdate_Returns200() throws Exception {
        // GIVEN: Service retorna subitem actualizado
        SubitemResponseDTO updatedDTO = new SubitemResponseDTO(
                1,
                1, // idTarea
                "Subitem actualizado",
                true,
                LocalDateTime.now()
        );
        when(subitemService.update(eq(1), any(UpdateSubitemDTO.class))).thenReturn(updatedDTO);

        // WHEN: PATCH actualización
        mockMvc.perform(patch("/api/tasks/1/subitems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSubitemDTO)))

        // THEN: HTTP 200 + subitem actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Subitem actualizado"))
                .andExpect(jsonPath("$.checked").value(true));

        verify(subitemService, times(1)).update(eq(1), any(UpdateSubitemDTO.class));
    }

    @Test
    @DisplayName("✅ PATCH /api/tasks/{idTarea}/subitems/{id} - Partial update with null fields")
    void update_PartialUpdateWithNulls_Returns200() throws Exception {
        // GIVEN: DTO parcial (solo actualizar checked)
        String partialJson = "{\"checked\":true}";
        SubitemResponseDTO partialDTO = new SubitemResponseDTO(
                1,
                1, // idTarea
                "Subitem de prueba", // descripción no cambió
                true,
                LocalDateTime.now()
        );
        when(subitemService.update(eq(1), any(UpdateSubitemDTO.class))).thenReturn(partialDTO);

        // WHEN: PATCH parcial
        mockMvc.perform(patch("/api/tasks/1/subitems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialJson))

        // THEN: HTTP 200
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true))
                .andExpect(jsonPath("$.description").value("Subitem de prueba"));

        verify(subitemService, times(1)).update(eq(1), any(UpdateSubitemDTO.class));
    }

    @Test
    @DisplayName("❌ PATCH /api/tasks/{idTarea}/subitems/{id} - Empty description returns 400")
    void update_EmptyDescription_Returns400() throws Exception {
        // GIVEN: DTO con descripción vacía
        String invalidJson = "{\"description\":\"\"}";

        // WHEN: PATCH con descripción vacía
        mockMvc.perform(patch("/api/tasks/1/subitems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(subitemService, never()).update(anyInt(), any(UpdateSubitemDTO.class));
    }

    @Test
    @DisplayName("❌ PATCH /api/tasks/{idTarea}/subitems/{id} - Non-existent subitem returns 404")
    void update_NonExistentSubitem_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(subitemService.update(eq(999), any(UpdateSubitemDTO.class)))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Subitem", "999"));

        // WHEN: PATCH subitem inexistente
        mockMvc.perform(patch("/api/tasks/1/subitems/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSubitemDTO)))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(subitemService, times(1)).update(eq(999), any(UpdateSubitemDTO.class));
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ DELETE /api/tasks/{idTarea}/subitems/{id} - Returns 204 no content")
    void delete_ExistingSubitem_Returns204() throws Exception {
        // GIVEN: Service elimina sin lanzar excepción
        doNothing().when(subitemService).delete(1);

        // WHEN: DELETE subitem
        mockMvc.perform(delete("/api/tasks/1/subitems/1"))

        // THEN: HTTP 204 (sin contenido)
                .andExpect(status().isNoContent());

        verify(subitemService, times(1)).delete(1);
    }

    @Test
    @DisplayName("❌ DELETE /api/tasks/{idTarea}/subitems/{id} - Non-existent subitem returns 404")
    void delete_NonExistentSubitem_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        doThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Subitem", "999"))
                .when(subitemService).delete(999);

        // WHEN: DELETE subitem inexistente
        mockMvc.perform(delete("/api/tasks/1/subitems/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(subitemService, times(1)).delete(999);
    }

    // ========================================
    // TESTS CONTENT TYPE
    // ========================================

    @Test
    @DisplayName("❌ POST /api/tasks/{idTarea}/subitems - Wrong content type returns 415")
    void create_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/tasks/{idTarea}/subitems - Invalid JSON returns 400")
    void create_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS PATH VARIABLES
    // ========================================

    @Test
    @DisplayName("✅ POST /api/tasks/{idTarea}/subitems - Valid task ID in path")
    void create_ValidTaskIdInPath_Returns201() throws Exception {
        // GIVEN: Service retorna subitem creado
        when(subitemService.create(eq(5), any(CreateSubitemDTO.class))).thenReturn(subitemResponseDTO);

        // WHEN: POST con ID de tarea válido
        mockMvc.perform(post("/api/tasks/5/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubitemDTO)))

        // THEN: HTTP 201
                .andExpect(status().isCreated());

        verify(subitemService, times(1)).create(eq(5), any(CreateSubitemDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tasks/{idTarea}/subitems - Invalid task ID returns 400")
    void create_InvalidTaskId_Returns400() throws Exception {
        // WHEN: POST con ID de tarea no numérico
        mockMvc.perform(post("/api/tasks/abc/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubitemDTO)))

        // THEN: HTTP 400 Bad Request
                .andExpect(status().isBadRequest());

        verify(subitemService, never()).create(anyInt(), any(CreateSubitemDTO.class));
    }

    // ========================================
    // TESTS CHECKED STATE
    // ========================================

    @Test
    @DisplayName("✅ PATCH /api/tasks/{idTarea}/subitems/{id} - Toggle checked state")
    void update_ToggleCheckedState_Returns200() throws Exception {
        // GIVEN: DTO para cambiar estado
        String toggleJson = "{\"checked\":true}";
        SubitemResponseDTO toggledDTO = new SubitemResponseDTO(
                1,
                1, // idTarea
                "Subitem de prueba",
                true, // ahora marcado
                LocalDateTime.now()
        );
        when(subitemService.update(eq(1), any(UpdateSubitemDTO.class))).thenReturn(toggledDTO);

        // WHEN: PATCH para marcar como completado
        mockMvc.perform(patch("/api/tasks/1/subitems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toggleJson))

        // THEN: HTTP 200 con estado actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true));

        verify(subitemService, times(1)).update(eq(1), any(UpdateSubitemDTO.class));
    }

    @Test
    @DisplayName("✅ POST /api/tasks/{idTarea}/subitems - Create with checked=true")
    void create_WithCheckedTrue_Returns201() throws Exception {
        // GIVEN: DTO con checked=true
        String checkedJson = "{\"description\":\"Ya completado\",\"checked\":true}";
        SubitemResponseDTO checkedDTO = new SubitemResponseDTO(
                1,
                1, // idTarea
                "Ya completado",
                true,
                LocalDateTime.now()
        );
        when(subitemService.create(eq(1), any(CreateSubitemDTO.class))).thenReturn(checkedDTO);

        // WHEN: POST con checked=true
        mockMvc.perform(post("/api/tasks/1/subitems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkedJson))

        // THEN: HTTP 201
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checked").value(true));

        verify(subitemService, times(1)).create(eq(1), any(CreateSubitemDTO.class));
    }
}
