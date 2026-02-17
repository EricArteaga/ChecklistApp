package com.example.checklistapp.auth.service;

import com.example.checklistapp.auth.dto.AuthResponseDTO;
import com.example.checklistapp.auth.dto.LoginDTO;
import com.example.checklistapp.auth.dto.RegisterDTO;
import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@ExtendWith(MockitoExtension.class)**: Habilita Mockito para JUnit 5
 * 2. **@Mock**: Crea simulacros de dependencias externas
 * 3. **@InjectMocks**: Crea AuthService e inyecta los mocks automáticamente
 * 4. **ReflectionTestUtils**: Inyecta valores de @Value fields (jwt secret, expiration)
 *
 * ESTRATEGIA DE TESTING:
 * ======================
 * - Test unitario puro: NO usamos @SpringBootTest
 * - Aislamiento: Mockeamos UserRepository y UserMapper
 * - Probamos LÓGICA DE AUTENTICACIÓN:
 *   - Validación de correo duplicado
 *   - Hash de contraseña con BCrypt
 *   - Generación de token JWT
 *   - Validación de credenciales
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // === MOCKS: Dependencias que simulamos ===
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    // === SYSTEM UNDER TEST (SUT): Lo que estamos probando ===
    @InjectMocks
    private AuthService authService;

    // === FIXTURE: Datos de prueba reutilizables ===
    private User testUser;
    private UserResponseDTO userResponseDTO;
    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;

    /**
     * @BeforeEach: Se ejecuta ANTES de CADA test
     * Inicializa los objetos de prueba y configura las propiedades de JWT
     */
    @BeforeEach
    void setUp() {
        // Configurar propiedades de JWT (simulando @Value injection)
        ReflectionTestUtils.setField(authService, "jwtSecret", "miSecretKeySuperSeguraParaTests123456789");
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L); // 1 hora

        // Crear usuario de prueba con hash BCrypt REAL para "password"
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode("password");

        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Test User");
        testUser.setHashContrasena(hashedPassword);  // Hash real de "password"

        // DTO de respuesta del usuario
        userResponseDTO = new UserResponseDTO(
            1,
            "test@example.com",
            "Test User",
            null  // fechaCreacion
        );

        // DTO de registro
        registerDTO = new RegisterDTO(
            "newuser@example.com",
            "New User",
            "password123"
        );

        // DTO de login
        loginDTO = new LoginDTO(
            "test@example.com",
            "password"  // Este password coincide con el hash BCrypt de testUser
        );
    }

    // ==================== TESTS DE REGISTER ====================

    @Test
    @DisplayName("✅ register() - Happy path: registro exitoso de nuevo usuario")
    void register_Success() {
        // GIVEN: El correo NO existe
        when(userRepository.existsByCorreo(registerDTO.correo())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Registramos el usuario
        AuthResponseDTO response = authService.register(registerDTO);

        // THEN: Retornamos AuthResponseDTO completo
        assertThat(response).isNotNull();
        assertThat(response.token()).isNotBlank();
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.user()).isNotNull();
        assertThat(response.user().correo()).isEqualTo(testUser.getCorreo());

        // Verify: Interacciones con mocks
        verify(userRepository).existsByCorreo(registerDTO.correo());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponseDTO(testUser);
    }

    @Test
    @DisplayName("❌ register() - Error: correo ya existe")
    void register_EmailAlreadyExists() {
        // GIVEN: El correo YA existe
        when(userRepository.existsByCorreo(registerDTO.correo())).thenReturn(true);

        // WHEN + THEN: Lanza BusinessRuleException
        assertThatThrownBy(() -> authService.register(registerDTO))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Ya existe un usuario con el correo");

        // Verify: NO se llamó a save porque falló validación
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("✅ register() - Verifica que la contraseña se hashea con BCrypt")
    void register_PasswordHashedWithBCrypt() {
        // GIVEN: El correo NO existe
        when(userRepository.existsByCorreo(registerDTO.correo())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            testUser.setHashContrasena(user.getHashContrasena()); // Capturar el hash
            return testUser;
        });
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Registramos el usuario
        authService.register(registerDTO);

        // THEN: La contraseña está hasheada (NO es el texto plano)
        assertThat(testUser.getHashContrasena())
            .isNotEqualTo(registerDTO.password())
            .startsWith("$2a$"); // Prefijo estándar de BCrypt

        // Verify: Se llamó a save con el usuario hasheado
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("✅ register() - El token JWT contiene el correo del usuario")
    void register_TokenContainsUserEmail() {
        // GIVEN: El correo NO existe
        when(userRepository.existsByCorreo(registerDTO.correo())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Registramos el usuario
        AuthResponseDTO response = authService.register(registerDTO);

        // THEN: Podemos validar el token y obtener el correo
        String emailFromToken = authService.validateTokenAndGetEmail(response.token());
        assertThat(emailFromToken).isEqualTo(testUser.getCorreo());
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    @DisplayName("✅ login() - Happy path: inicio de sesión exitoso")
    void login_Success() {
        // GIVEN: Usuario existe y contraseña coincide
        when(userRepository.findByCorreo(loginDTO.correo())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Iniciamos sesión
        AuthResponseDTO response = authService.login(loginDTO);

        // THEN: Retornamos AuthResponseDTO completo
        assertThat(response).isNotNull();
        assertThat(response.token()).isNotBlank();
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.user()).isNotNull();
        assertThat(response.user().correo()).isEqualTo(testUser.getCorreo());

        // Verify
        verify(userRepository).findByCorreo(loginDTO.correo());
        verify(userMapper).toResponseDTO(testUser);
    }

    @Test
    @DisplayName("❌ login() - Error: usuario no existe")
    void login_UserNotFound() {
        // GIVEN: Usuario NO existe
        when(userRepository.findByCorreo(loginDTO.correo())).thenReturn(Optional.empty());

        // WHEN + THEN: Lanza BusinessRuleException
        assertThatThrownBy(() -> authService.login(loginDTO))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Credenciales inválidas");

        // Verify: NO se generó token
        verify(userMapper, never()).toResponseDTO(any(User.class));
    }

    @Test
    @DisplayName("❌ login() - Error: contraseña incorrecta")
    void login_WrongPassword() {
        // GIVEN: Usuario existe pero contraseña es incorrecta
        // Generar hash BCrypt para "wrongPassword" (diferente a "password")
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String wrongHash = passwordEncoder.encode("wrongPassword");

        User userWithWrongPassword = new User();
        userWithWrongPassword.setId(1);
        userWithWrongPassword.setCorreo(loginDTO.correo());
        userWithWrongPassword.setNombre("Test User");
        userWithWrongPassword.setHashContrasena(wrongHash);

        when(userRepository.findByCorreo(loginDTO.correo())).thenReturn(Optional.of(userWithWrongPassword));

        // WHEN + THEN: Lanza BusinessRuleException
        assertThatThrownBy(() -> authService.login(loginDTO))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    @DisplayName("✅ login() - El token JWT contiene el correo del usuario")
    void login_TokenContainsUserEmail() {
        // GIVEN: Usuario existe y contraseña coincide
        when(userRepository.findByCorreo(loginDTO.correo())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Iniciamos sesión
        AuthResponseDTO response = authService.login(loginDTO);

        // THEN: Podemos validar el token y obtener el correo
        String emailFromToken = authService.validateTokenAndGetEmail(response.token());
        assertThat(emailFromToken).isEqualTo(testUser.getCorreo());
    }

    // ==================== TESTS DE VALIDAR TOKEN ====================

    @Test
    @DisplayName("✅ validateTokenAndGetEmail() - Happy path: token válido")
    void validateTokenAndGetEmail_ValidToken() {
        // GIVEN: Generamos un token válido
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);
        AuthResponseDTO response = authService.login(loginDTO);
        String validToken = response.token();

        // WHEN: Validamos el token
        String email = authService.validateTokenAndGetEmail(validToken);

        // THEN: Obtenemos el correo del usuario
        assertThat(email).isEqualTo(testUser.getCorreo());
    }

    @Test
    @DisplayName("❌ validateTokenAndGetEmail() - Error: token mal formado")
    void validateTokenAndGetEmail_InvalidToken() {
        // GIVEN: Un token mal formado
        String invalidToken = "invalid.token.here";

        // WHEN + THEN: Lanza excepción de JWT
        assertThatThrownBy(() -> authService.validateTokenAndGetEmail(invalidToken))
            .isInstanceOf(io.jsonwebtoken.MalformedJwtException.class);
    }

    @Test
    @DisplayName("❌ validateTokenAndGetEmail() - Error: token con firma inválida")
    void validateTokenAndGetEmail_InvalidSignature() {
        // GIVEN: Un token con firma inválida (secret key diferente)
        String tokenWithWrongSignature = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJleHAiOjk5OTk5OTk5OTl9.invalid";

        // WHEN + THEN: Lanza excepción de firma inválida
        assertThatThrownBy(() -> authService.validateTokenAndGetEmail(tokenWithWrongSignature))
            .isInstanceOf(io.jsonwebtoken.SignatureException.class);
    }

    // ==================== TESTS DE SEGURIDAD ====================

    @Test
    @DisplayName("✅ register() - Múltiples usuarios con mismos datos generan tokens diferentes")
    void register_DifferentTokensForDifferentUsers() {
        // GIVEN: Dos usuarios diferentes
        RegisterDTO dto1 = new RegisterDTO("user1@example.com", "User 1", "pass123");
        RegisterDTO dto2 = new RegisterDTO("user2@example.com", "User 2", "pass456");

        User user1 = new User();
        user1.setId(1);
        user1.setCorreo("user1@example.com");
        user1.setNombre("User 1");

        User user2 = new User();
        user2.setId(2);
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");

        UserResponseDTO response1 = new UserResponseDTO(1, "user1@example.com", "User 1", null);
        UserResponseDTO response2 = new UserResponseDTO(2, "user2@example.com", "User 2", null);

        when(userRepository.existsByCorreo(dto1.correo())).thenReturn(false);
        when(userRepository.existsByCorreo(dto2.correo())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user1, user2);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(response1, response2);

        // WHEN: Registramos ambos usuarios
        AuthResponseDTO auth1 = authService.register(dto1);
        AuthResponseDTO auth2 = authService.register(dto2);

        // THEN: Los tokens son diferentes (tienen diferente subject)
        assertThat(auth1.token()).isNotEqualTo(auth2.token());
    }

    @Test
    @DisplayName("✅ login() - Mismo usuario en diferentes logins genera tokens válidos")
    void login_DifferentTokensEachLogin() {
        // GIVEN: Usuario existe
        when(userRepository.findByCorreo(loginDTO.correo())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(userResponseDTO);

        // WHEN: Iniciamos sesión dos veces
        AuthResponseDTO auth1 = authService.login(loginDTO);
        AuthResponseDTO auth2 = authService.login(loginDTO);

        // THEN: Ambos tokens son válidos y tienen el mismo subject (correo)
        String email1 = authService.validateTokenAndGetEmail(auth1.token());
        String email2 = authService.validateTokenAndGetEmail(auth2.token());

        assertThat(email1).isEqualTo(testUser.getCorreo());
        assertThat(email2).isEqualTo(testUser.getCorreo());
        assertThat(auth1.token()).isNotBlank();
        assertThat(auth2.token()).isNotBlank();
    }
}
