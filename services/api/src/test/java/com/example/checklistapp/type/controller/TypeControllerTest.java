package com.example.checklistapp.type.controller;

import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.service.TypeService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TypeController
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@WebMvcTest**: Prueba solo el controller con contexto web
 * 2. **@MockBean**: Mock del servicio para aislar el controller
 * 3. **MockMvc**: Simula peticiones HTTP y verifica respuestas
 *
 * QUÉ PROBAMOS:
 * =============
 * - Status codes HTTP
 * - Estructura JSON de respuestas
 * - Validaciones de entrada
 * - Routing de endpoints
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TypeController.class)
@DisplayName("TypeController Integration Tests")
class TypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TypeService typeService;

    // === FIXTURE: DTOs de prueba ===
    private TypeResponseDTO typeResponseDTO;
    private TypeSummaryDTO typeSummaryDTO;
    private CreateTypeDTO createTypeDTO;
    private UpdateTypeDTO updateTypeDTO;

    @BeforeEach
    void setUp() {
        // TypeResponseDTO: id, idUsuario, nombre, color
        typeResponseDTO = new TypeResponseDTO(
                1,
                1, // idUsuario
                "Trabajo",
                "#326cc3"
        );

        // TypeSummaryDTO: id, nombre, color
        typeSummaryDTO = new TypeSummaryDTO(
                1,
                "Trabajo",
                "#326cc3"
        );

        // CreateTypeDTO: idUsuario, nombre, color
        createTypeDTO = new CreateTypeDTO(
                1, // idUsuario
                "Personal",
                "#28a745"
        );

        // UpdateTypeDTO: nombre, color (ambos opcionales)
        updateTypeDTO = new UpdateTypeDTO(
                "Deporte",
                "#dc3545"
        );
    }

    // ========================================
    // TESTS CREATE
    // ========================================

    @Test
    @DisplayName("✅ POST /api/tipos - Valid type returns 201")
    void create_ValidType_Returns201() throws Exception {
        // GIVEN: Service retorna tipo creado
        when(typeService.create(any(CreateTypeDTO.class))).thenReturn(typeResponseDTO);

        // WHEN: POST tipo válido
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTypeDTO)))

        // THEN: HTTP 201 + JSON con tipo
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Trabajo"))
                .andExpect(jsonPath("$.color").value("#326cc3"))
                .andExpect(jsonPath("$.idUsuario").value(1));

        verify(typeService, times(1)).create(any(CreateTypeDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tipos - Empty name returns 400")
    void create_EmptyName_Returns400() throws Exception {
        // GIVEN: DTO con nombre vacío
        String invalidJson = "{\"nombre\":\"\",\"color\":\"#fff\",\"idUsuario\":1}";

        // WHEN: POST con nombre vacío
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(typeService, never()).create(any(CreateTypeDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tipos - Missing required fields returns 400")
    void create_MissingRequiredFields_Returns400() throws Exception {
        // GIVEN: JSON incompleto
        String incompleteJson = "{\"nombre\":\"Trabajo\"}";

        // WHEN: POST sin idUsuario
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(typeService, never()).create(any(CreateTypeDTO.class));
    }

    @Test
    @DisplayName("✅ POST /api/tipos - Null color is valid")
    void create_NullColor_Returns201() throws Exception {
        // GIVEN: DTO sin color (color es opcional)
        String jsonWithoutColor = "{\"nombre\":\"Sin color\",\"idUsuario\":1}";
        TypeResponseDTO responseWithoutColor = new TypeResponseDTO(1, 1, "Sin color", null);
        when(typeService.create(any(CreateTypeDTO.class))).thenReturn(responseWithoutColor);

        // WHEN: POST sin color
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutColor))

        // THEN: HTTP 201
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Sin color"))
                .andExpect(jsonPath("$.color").isEmpty());
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tipos/{id} - Existing type returns 200")
    void findById_ExistingType_Returns200() throws Exception {
        // GIVEN: Service retorna tipo
        when(typeService.findById(1)).thenReturn(typeResponseDTO);

        // WHEN: GET tipo por ID
        mockMvc.perform(get("/api/tipos/1"))

        // THEN: HTTP 200 + JSON con tipo
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Trabajo"))
                .andExpect(jsonPath("$.color").value("#326cc3"));

        verify(typeService, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ GET /api/tipos/{id} - Non-existent type returns 404")
    void findById_NonExistentType_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(typeService.findById(999))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Tipo", "999"));

        // WHEN: GET tipo inexistente
        mockMvc.perform(get("/api/tipos/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(typeService, times(1)).findById(999);
    }

    // ========================================
    // TESTS FIND ALL
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tipos - Returns 200 with type list")
    void findAll_TypesExist_Returns200WithList() throws Exception {
        // GIVEN: Service retorna lista
        when(typeService.findAll()).thenReturn(List.of(typeSummaryDTO));

        // WHEN: GET todos los tipos
        mockMvc.perform(get("/api/tipos"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Trabajo"))
                .andExpect(jsonPath("$[0].color").value("#326cc3"));

        verify(typeService, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ GET /api/tipos - Empty list returns 200 with empty array")
    void findAll_NoTypes_Returns200WithEmptyArray() throws Exception {
        // GIVEN: Service retorna lista vacía
        when(typeService.findAll()).thenReturn(Collections.emptyList());

        // WHEN: GET tipos (no hay ninguno)
        mockMvc.perform(get("/api/tipos"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS FIND BY USUARIO
    // ========================================

    @Test
    @DisplayName("✅ GET /api/tipos/usuario/{id} - Returns user types")
    void findByIdUsuario_ExistingUser_Returns200WithTypes() throws Exception {
        // GIVEN: Service retorna tipos del usuario
        when(typeService.findByIdUsuario(1)).thenReturn(List.of(typeSummaryDTO));

        // WHEN: GET tipos por usuario
        mockMvc.perform(get("/api/tipos/usuario/1"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Trabajo"));

        verify(typeService, times(1)).findByIdUsuario(1);
    }

    @Test
    @DisplayName("✅ GET /api/tipos/usuario/{id} - Non-existent user returns empty list")
    void findByIdUsuario_NonExistentUser_Returns200WithEmptyArray() throws Exception {
        // GIVEN: Service retorna lista vacía
        when(typeService.findByIdUsuario(999)).thenReturn(Collections.emptyList());

        // WHEN: GET tipos de usuario inexistente
        mockMvc.perform(get("/api/tipos/usuario/999"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ PATCH /api/tipos/{id} - Valid update returns 200")
    void update_ValidUpdate_Returns200() throws Exception {
        // GIVEN: Service retorna tipo actualizado
        TypeResponseDTO updatedDTO = new TypeResponseDTO(
                1,
                1, // idUsuario
                "Deporte",
                "#dc3545"
        );
        when(typeService.update(eq(1), any(UpdateTypeDTO.class))).thenReturn(updatedDTO);

        // WHEN: PATCH actualización
        mockMvc.perform(patch("/api/tipos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTypeDTO)))

        // THEN: HTTP 200 + tipo actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Deporte"))
                .andExpect(jsonPath("$.color").value("#dc3545"));

        verify(typeService, times(1)).update(eq(1), any(UpdateTypeDTO.class));
    }

    @Test
    @DisplayName("✅ PATCH /api/tipos/{id} - Partial update with null color")
    void update_PartialUpdateWithNullColor_Returns200() throws Exception {
        // GIVEN: DTO parcial (solo actualizar nombre)
        String partialJson = "{\"nombre\":\"Solo Nombre\"}";
        TypeResponseDTO partialDTO = new TypeResponseDTO(
                1,
                1, // idUsuario
                "Solo Nombre",
                "#326cc3" // color no cambió
        );
        when(typeService.update(eq(1), any(UpdateTypeDTO.class))).thenReturn(partialDTO);

        // WHEN: PATCH parcial
        mockMvc.perform(patch("/api/tipos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialJson))

        // THEN: HTTP 200
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Solo Nombre"))
                .andExpect(jsonPath("$.color").value("#326cc3"));

        verify(typeService, times(1)).update(eq(1), any(UpdateTypeDTO.class));
    }

    @Test
    @DisplayName("❌ PATCH /api/tipos/{id} - Empty name returns 400")
    void update_EmptyName_Returns400() throws Exception {
        // GIVEN: DTO con nombre vacío
        String invalidJson = "{\"nombre\":\"\"}";

        // WHEN: PATCH con nombre vacío
        mockMvc.perform(patch("/api/tipos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(typeService, never()).update(anyInt(), any(UpdateTypeDTO.class));
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ DELETE /api/tipos/{id} - Returns 204 no content")
    void delete_ExistingType_Returns204() throws Exception {
        // GIVEN: Service elimina sin lanzar excepción
        doNothing().when(typeService).delete(1);

        // WHEN: DELETE tipo
        mockMvc.perform(delete("/api/tipos/1"))

        // THEN: HTTP 204 (sin contenido)
                .andExpect(status().isNoContent());

        verify(typeService, times(1)).delete(1);
    }

    @Test
    @DisplayName("❌ DELETE /api/tipos/{id} - Non-existent type returns 404")
    void delete_NonExistentType_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        doThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Tipo", "999"))
                .when(typeService).delete(999);

        // WHEN: DELETE tipo inexistente
        mockMvc.perform(delete("/api/tipos/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(typeService, times(1)).delete(999);
    }

    // ========================================
    // TESTS CONTENT TYPE
    // ========================================

    @Test
    @DisplayName("❌ POST /api/tipos - Wrong content type returns 415")
    void create_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/tipos - Invalid JSON returns 400")
    void create_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS COLOR VALIDATION
    // ========================================

    @Test
    @DisplayName("✅ POST /api/tipos - Valid hex color returns 201")
    void create_ValidHexColor_Returns201() throws Exception {
        // GIVEN: DTO con color hex válido
        String validColorJson = "{\"nombre\":\"Test\",\"color\":\"#ABC123\",\"idUsuario\":1}";
        when(typeService.create(any(CreateTypeDTO.class))).thenReturn(typeResponseDTO);

        // WHEN: POST con color hex válido
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validColorJson))

        // THEN: HTTP 201
                .andExpect(status().isCreated());

        verify(typeService, times(1)).create(any(CreateTypeDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/tipos - Invalid color format returns 400")
    void create_InvalidColorFormat_Returns400() throws Exception {
        // GIVEN: DTO con color inválido (demasiado largo)
        String invalidColorJson = "{\"nombre\":\"Test\",\"color\":\"#123456789\",\"idUsuario\":1}";

        // WHEN: POST con color inválido
        mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidColorJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(typeService, never()).create(any(CreateTypeDTO.class));
    }
}
