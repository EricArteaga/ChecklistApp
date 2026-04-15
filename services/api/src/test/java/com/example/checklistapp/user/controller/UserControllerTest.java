package com.example.checklistapp.user.controller;

import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.service.UserService;
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
 * Integration tests for UserController
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
@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // === FIXTURE: DTOs de prueba ===
    private UserResponseDTO userResponseDTO;
    private UserSummaryDTO userSummaryDTO;
    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    void setUp() {
        // UserResponseDTO: id, correo, nombre, fechaCreacion
        userResponseDTO = new UserResponseDTO(
                1,
                "test@example.com",
                "Usuario Test",
                LocalDateTime.now()
        );

        // UserSummaryDTO: id, correo, nombre
        userSummaryDTO = new UserSummaryDTO(
                1,
                "test@example.com",
                "Usuario Test"
        );

        // CreateUserDTO: correo, nombre (no password field)
        createUserDTO = new CreateUserDTO(
                "newuser@example.com",
                "Nuevo Usuario"
        );

        // UpdateUserDTO: correo, nombre (ambos opcionales)
        updateUserDTO = new UpdateUserDTO(
                "updated@example.com",
                "Usuario Actualizado"
        );
    }

    // ========================================
    // TESTS CREATE
    // ========================================

    @Test
    @DisplayName("✅ POST /api/usuarios - Valid user returns 201")
    void create_ValidUser_Returns201() throws Exception {
        // GIVEN: Service retorna usuario creado
        when(userService.create(any(CreateUserDTO.class))).thenReturn(userResponseDTO);

        // WHEN: POST usuario válido
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))

        // THEN: HTTP 201 + JSON con usuario
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.nombre").value("Usuario Test"));

        verify(userService, times(1)).create(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/usuarios - Invalid email returns 400")
    void create_InvalidEmail_Returns400() throws Exception {
        // GIVEN: DTO con email inválido
        String invalidJson = "{\"correo\":\"not-an-email\",\"nombre\":\"Test\",\"contrasena\":\"pass\"}";

        // WHEN: POST con email inválido
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/usuarios - Missing required fields returns 400")
    void create_MissingRequiredFields_Returns400() throws Exception {
        // GIVEN: JSON incompleto
        String incompleteJson = "{\"correo\":\"test@example.com\"}";

        // WHEN: POST sin todos los campos
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/usuarios - Empty password returns 400")
    void create_EmptyPassword_Returns400() throws Exception {
        // GIVEN: DTO con contraseña vacía
        String weakPassJson = "{\"correo\":\"test@example.com\",\"nombre\":\"Test\",\"contrasena\":\"\"}";

        // WHEN: POST con contraseña vacía
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weakPassJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ GET /api/usuarios/{id} - Existing user returns 200")
    void findById_ExistingUser_Returns200() throws Exception {
        // GIVEN: Service retorna usuario
        when(userService.findById(1)).thenReturn(userResponseDTO);

        // WHEN: GET usuario por ID
        mockMvc.perform(get("/api/usuarios/1"))

        // THEN: HTTP 200 + JSON con usuario
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.nombre").value("Usuario Test"));

        verify(userService, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ GET /api/usuarios/{id} - Non-existent user returns 404")
    void findById_NonExistentUser_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        when(userService.findById(999))
                .thenThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Usuario", "999"));

        // WHEN: GET usuario inexistente
        mockMvc.perform(get("/api/usuarios/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(999);
    }

    // ========================================
    // TESTS FIND ALL
    // ========================================

    @Test
    @DisplayName("✅ GET /api/usuarios - Returns 200 with user list")
    void findAll_UsersExist_Returns200WithList() throws Exception {
        // GIVEN: Service retorna lista
        when(userService.findAll()).thenReturn(List.of(userSummaryDTO));

        // WHEN: GET todos los usuarios
        mockMvc.perform(get("/api/usuarios"))

        // THEN: HTTP 200 + JSON array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].correo").value("test@example.com"));

        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ GET /api/usuarios - Empty list returns 200 with empty array")
    void findAll_NoUsers_Returns200WithEmptyArray() throws Exception {
        // GIVEN: Service retorna lista vacía
        when(userService.findAll()).thenReturn(Collections.emptyList());

        // WHEN: GET usuarios (no hay ninguno)
        mockMvc.perform(get("/api/usuarios"))

        // THEN: HTTP 200 + array vacío
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ PATCH /api/usuarios/{id} - Valid update returns 200")
    void update_ValidUpdate_Returns200() throws Exception {
        // GIVEN: Service retorna usuario actualizado
        UserResponseDTO updatedDTO = new UserResponseDTO(
                1,
                "updated@example.com",
                "Usuario Actualizado",
                LocalDateTime.now()
        );
        when(userService.update(eq(1), any(UpdateUserDTO.class))).thenReturn(updatedDTO);

        // WHEN: PATCH actualización
        mockMvc.perform(patch("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))

        // THEN: HTTP 200 + usuario actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("updated@example.com"))
                .andExpect(jsonPath("$.nombre").value("Usuario Actualizado"));

        verify(userService, times(1)).update(eq(1), any(UpdateUserDTO.class));
    }

    @Test
    @DisplayName("✅ PATCH /api/usuarios/{id} - Partial update with null fields")
    void update_PartialUpdateWithNulls_Returns200() throws Exception {
        // GIVEN: DTO parcial (solo actualizar nombre)
        String partialJson = "{\"nombre\":\"Solo Nombre\"}";
        UserResponseDTO partialDTO = new UserResponseDTO(
                1,
                "test@example.com", // correo no cambió
                "Solo Nombre",
                LocalDateTime.now()
        );
        when(userService.update(eq(1), any(UpdateUserDTO.class))).thenReturn(partialDTO);

        // WHEN: PATCH parcial
        mockMvc.perform(patch("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialJson))

        // THEN: HTTP 200
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Solo Nombre"))
                .andExpect(jsonPath("$.correo").value("test@example.com"));

        verify(userService, times(1)).update(eq(1), any(UpdateUserDTO.class));
    }

    @Test
    @DisplayName("❌ PATCH /api/usuarios/{id} - Invalid email format returns 400")
    void update_InvalidEmailFormat_Returns400() throws Exception {
        // GIVEN: DTO con email inválido
        String invalidJson = "{\"correo\":\"not-an-email\"}";

        // WHEN: PATCH con email inválido
        mockMvc.perform(patch("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(anyInt(), any(UpdateUserDTO.class));
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ DELETE /api/usuarios/{id} - Returns 204 no content")
    void delete_ExistingUser_Returns204() throws Exception {
        // GIVEN: Service elimina sin lanzar excepción
        doNothing().when(userService).delete(1);

        // WHEN: DELETE usuario
        mockMvc.perform(delete("/api/usuarios/1"))

        // THEN: HTTP 204 (sin contenido)
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1);
    }

    @Test
    @DisplayName("❌ DELETE /api/usuarios/{id} - Non-existent user returns 404")
    void delete_NonExistentUser_Returns404() throws Exception {
        // GIVEN: Service lanza ResourceNotFoundException
        doThrow(new com.example.checklistapp.common.exception.ResourceNotFoundException("Usuario", "999"))
                .when(userService).delete(999);

        // WHEN: DELETE usuario inexistente
        mockMvc.perform(delete("/api/usuarios/999"))

        // THEN: HTTP 404
                .andExpect(status().isNotFound());

        verify(userService, times(1)).delete(999);
    }

    // ========================================
    // TESTS CONTENT TYPE
    // ========================================

    @Test
    @DisplayName("❌ POST /api/usuarios - Wrong content type returns 415")
    void create_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/usuarios - Invalid JSON returns 400")
    void create_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS PATH VARIABLES
    // ========================================

    @Test
    @DisplayName("✅ GET /api/usuarios/{id} - Valid numeric ID")
    void findById_ValidNumericId_Returns200() throws Exception {
        // GIVEN: Service retorna usuario
        when(userService.findById(1)).thenReturn(userResponseDTO);

        // WHEN: GET con ID numérico válido
        mockMvc.perform(get("/api/usuarios/1"))

        // THEN: HTTP 200
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ GET /api/usuarios/{id} - Invalid ID type returns 400")
    void findById_InvalidIdType_Returns400() throws Exception {
        // WHEN: GET con ID no numérico
        mockMvc.perform(get("/api/usuarios/abc"))

        // THEN: HTTP 400 Bad Request
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(anyInt());
    }
}
