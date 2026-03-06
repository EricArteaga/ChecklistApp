package com.example.checklistapp.auth.controller;

import com.example.checklistapp.auth.dto.AuthResponseDTO;
import com.example.checklistapp.auth.dto.LoginDTO;
import com.example.checklistapp.auth.dto.RegisterDTO;
import com.example.checklistapp.auth.service.AuthService;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@WebMvcTest**: Prueba solo el controller con contexto web
 * 2. **@MockBean**: Mocks del servicio y repositorio para aislar el controller
 * 3. **MockMvc**: Simula peticiones HTTP y verifica respuestas
 *
 * QUÉ PROBAMOS:
 * =============
 * - Endpoints de autenticación (login, register)
 * - Health check endpoint
 * - Validaciones de credenciales
 * - Estructura de JWT en respuesta
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // Deshabilitar filtros de seguridad para tests
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper userMapper;

    // === FIXTURE: DTOs de prueba ===
    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private AuthResponseDTO authResponseDTO;
    private User testUser;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        // RegisterDTO: correo, nombre, contrasena
        registerDTO = new RegisterDTO(
                "newuser@example.com",
                "New User",
                "password123"
        );

        // LoginDTO: correo, contrasena
        loginDTO = new LoginDTO(
                "test@example.com",
                "password123"
        );

        // AuthResponseDTO: token, refreshToken, user, type
        authResponseDTO = new AuthResponseDTO(
                "fake-jwt-token",
                "fake-refresh-token",
                new UserResponseDTO(
                        1,
                        "test@example.com",
                        "Test User",
                        java.time.LocalDateTime.now()
                ),
                "Bearer"
        );

        // User entity
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Test User");
        testUser.setHashContrasena("hashedpassword");
        testUser.setFechaCreacion(java.time.LocalDateTime.now());

        // UserResponseDTO
        userResponseDTO = new UserResponseDTO(
                1,
                "test@example.com",
                "Test User",
                java.time.LocalDateTime.now()
        );
    }

    // ========================================
    // TESTS REGISTER
    // ========================================

    @Test
    @DisplayName("✅ POST /api/auth/register - Valid registration returns 201")
    void register_ValidRegistration_Returns201() throws Exception {
        // GIVEN: Service retorna respuesta de registro
        when(authService.register(any(RegisterDTO.class))).thenReturn(authResponseDTO);

        // WHEN: POST registro válido
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))

        // THEN: HTTP 201 + JSON con token y usuario
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.correo").value("test@example.com"))
                .andExpect(jsonPath("$.user.nombre").value("Test User"));

        verify(authService, times(1)).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/register - Invalid email returns 400")
    void register_InvalidEmail_Returns400() throws Exception {
        // GIVEN: DTO con email inválido
        String invalidJson = "{\"correo\":\"not-an-email\",\"contrasena\":\"pass\",\"nombre\":\"Test\"}";

        // WHEN: POST con email inválido
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/register - Weak password returns 400")
    void register_WeakPassword_Returns400() throws Exception {
        // GIVEN: DTO con contraseña débil
        String weakPassJson = "{\"correo\":\"test@example.com\",\"contrasena\":\"123\",\"nombre\":\"Test\"}";

        // WHEN: POST con contraseña débil
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weakPassJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/register - Missing fields returns 400")
    void register_MissingFields_Returns400() throws Exception {
        // GIVEN: JSON incompleto
        String incompleteJson = "{\"correo\":\"test@example.com\"}";

        // WHEN: POST sin todos los campos
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterDTO.class));
    }

    // ========================================
    // TESTS LOGIN
    // ========================================

    @Test
    @DisplayName("✅ POST /api/auth/login - Valid credentials returns 200")
    void login_ValidCredentials_Returns200() throws Exception {
        // GIVEN: Service retorna respuesta de login
        when(authService.login(any(LoginDTO.class))).thenReturn(authResponseDTO);

        // WHEN: POST login válido
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))

        // THEN: HTTP 200 + JSON con token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.correo").value("test@example.com"));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/login - Invalid email returns 400")
    void login_InvalidEmail_Returns400() throws Exception {
        // GIVEN: DTO con email inválido
        String invalidJson = "{\"correo\":\"not-an-email\",\"contrasena\":\"pass\"}";

        // WHEN: POST con email inválido
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/login - Missing password returns 400")
    void login_MissingPassword_Returns400() throws Exception {
        // GIVEN: JSON sin contraseña
        String incompleteJson = "{\"correo\":\"test@example.com\"}";

        // WHEN: POST sin contraseña
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }

    // ========================================
    // TESTS HEALTH CHECK
    // ========================================

    @Test
    @DisplayName("✅ GET /api/auth/health - Returns 200 with status UP")
    void health_Returns200WithStatusUp() throws Exception {
        // WHEN: GET health check
        mockMvc.perform(get("/api/auth/health"))

        // THEN: HTTP 200 + JSON con status
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("ChecklistApp API"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("✅ GET /api/auth/health - No authentication required")
    void health_NoAuthenticationRequired_Returns200() throws Exception {
        // WHEN: GET health check sin token
        mockMvc.perform(get("/api/auth/health")
                        .header("Authorization", "")) // Sin token

        // THEN: HTTP 200 (endpoint público)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ========================================
    // TESTS GET CURRENT USER (/me)
    // ========================================

    @Test
    @DisplayName("✅ GET /api/auth/me - Valid token returns user")
    void getCurrentUser_ValidToken_Returns200() throws Exception {
        // GIVEN: Token válido y usuario existe
        String token = "valid-jwt-token";
        when(authService.validateTokenAndGetEmail(token)).thenReturn("test@example.com");
        when(userRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: GET usuario actual con token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))

        // THEN: HTTP 200 + datos del usuario
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.nombre").value("Test User"));

        verify(authService, times(1)).validateTokenAndGetEmail(token);
        verify(userRepository, times(1)).findByCorreo("test@example.com");
    }

    @Test
    @DisplayName("❌ GET /api/auth/me - Missing Authorization header returns 400")
    void getCurrentUser_MissingAuthorizationHeader_Returns400() throws Exception {
        // WHEN: GET sin header Authorization
        mockMvc.perform(get("/api/auth/me"))

        // THEN: HTTP 400 (Bad Request - header faltante)
                .andExpect(status().isBadRequest());

        verify(authService, never()).validateTokenAndGetEmail(anyString());
    }

    @Test
    @DisplayName("❌ GET /api/auth/me - Invalid token format returns 400")
    void getCurrentUser_InvalidTokenFormat_Returns400() throws Exception {
        // GIVEN: Token sin "Bearer " prefix
        // WHEN: GET con formato inválido
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "invalid-format"))

        // THEN: HTTP 400 (error al procesar token)
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS CONTENT TYPE
    // ========================================

    @Test
    @DisplayName("❌ POST /api/auth/register - Wrong content type returns 415")
    void register_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/auth/login - Wrong content type returns 415")
    void login_WrongContentType_Returns415() throws Exception {
        // WHEN: POST con content type incorrecto
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("test"))

        // THEN: HTTP 415 Unsupported Media Type
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("❌ POST /api/auth/register - Invalid JSON returns 400")
    void register_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ POST /api/auth/login - Invalid JSON returns 400")
    void login_InvalidJson_Returns400() throws Exception {
        // WHEN: POST con JSON malformado
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // TESTS EMAIL FORMAT VALIDATION
    // ========================================

    @Test
    @DisplayName("❌ POST /api/auth/register - Empty email returns 400")
    void register_EmptyEmail_Returns400() throws Exception {
        // GIVEN: DTO con email vacío
        String emptyEmailJson = "{\"correo\":\"\",\"contrasena\":\"password123\",\"nombre\":\"Test\"}";

        // WHEN: POST con email vacío
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyEmailJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterDTO.class));
    }

    @Test
    @DisplayName("❌ POST /api/auth/login - Empty email returns 400")
    void login_EmptyEmail_Returns400() throws Exception {
        // GIVEN: DTO con email vacío
        String emptyEmailJson = "{\"correo\":\"\",\"contrasena\":\"password123\"}";

        // WHEN: POST con email vacío
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyEmailJson))

        // THEN: HTTP 400
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginDTO.class));
    }
}
