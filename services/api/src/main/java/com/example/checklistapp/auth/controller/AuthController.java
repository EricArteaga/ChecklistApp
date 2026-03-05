package com.example.checklistapp.auth.controller;

import com.example.checklistapp.auth.dto.AuthResponseDTO;
import com.example.checklistapp.auth.dto.LoginDTO;
import com.example.checklistapp.auth.dto.RegisterDTO;
import com.example.checklistapp.auth.service.AuthService;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de autenticación
 *
 * Endpoints públicos (no requieren autenticación):
 * - POST /api/auth/register - Registro
 * - POST /api/auth/login - Login
 * - GET /api/auth/health - Health check (verificar si el backend está corriendo)
 *
 * Endpoints protegidos (requieren JWT):
 * - GET /api/auth/me - Obtener usuario actual
 *
 * Nota: CORS se maneja globalmente en SecurityConfig, no se requiere @CrossOrigin
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthController(
            AuthService authService,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Registrar un nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        AuthResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Iniciar sesión
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check - Verificar si el backend está corriendo
     * GET /api/auth/health
     * Endpoint público (no requiere autenticación)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ChecklistApp API");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener usuario autenticado actual
     * GET /api/auth/me
     * Requiere header: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // Extraer token del header "Bearer <token>"
        String token = authorizationHeader.replace("Bearer ", "");

        // Validar token y obtener correo
        String correo = authService.validateTokenAndGetEmail(token);

        // Buscar usuario
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", correo));

        // Convertir a DTO
        UserResponseDTO userResponseDTO = userMapper.toResponseDTO(user);

        return ResponseEntity.ok(userResponseDTO);
    }
}
