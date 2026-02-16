package com.example.checklistapp.auth.service;

import com.example.checklistapp.auth.dto.AuthResponseDTO;
import com.example.checklistapp.auth.dto.LoginDTO;
import com.example.checklistapp.auth.dto.RegisterDTO;
import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio de autenticación
 *
 * Responsabilidades:
 * - Registrar nuevos usuarios (con hash de contraseña)
 * - Iniciar sesión (validar credenciales)
 * - Generar tokens JWT
 * - Validar tokens JWT
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long jwtExpirationMs;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Registrar un nuevo usuario
     *
     * @param dto DTO con datos de registro
     * @return AuthResponseDTO con token y datos del usuario
     */
    public AuthResponseDTO register(RegisterDTO dto) {
        // 1. Verificar que el correo no existe
        if (userRepository.existsByCorreo(dto.correo())) {
            throw new BusinessRuleException("Ya existe un usuario con el correo: " + dto.correo());
        }

        // 2. Crear usuario con contraseña hasheada
        User user = new User();
        user.setCorreo(dto.correo());
        user.setNombre(dto.nombre());
        user.setHashContrasena(passwordEncoder.encode(dto.password()));  // Hash BCrypt

        // 3. Guardar usuario
        User savedUser = userRepository.save(user);

        // 4. Generar token JWT
        String token = generateToken(savedUser);

        // 5. Convertir a DTO
        UserResponseDTO userResponseDTO = userMapper.toResponseDTO(savedUser);

        // 6. Retornar respuesta
        return new AuthResponseDTO(
                token,
                null,  // TODO: Implementar refresh token
                userResponseDTO,
                "Bearer"
        );
    }

    /**
     * Iniciar sesión
     *
     * @param dto DTO con credenciales
     * @return AuthResponseDTO con token y datos del usuario
     */
    public AuthResponseDTO login(LoginDTO dto) {
        // 1. Buscar usuario por correo
        User user = userRepository.findByCorreo(dto.correo())
                .orElseThrow(() -> new BusinessRuleException("Credenciales inválidas"));

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(dto.password(), user.getHashContrasena())) {
            throw new BusinessRuleException("Credenciales inválidas");
        }

        // 3. Generar token JWT
        String token = generateToken(user);

        // 4. Convertir a DTO
        UserResponseDTO userResponseDTO = userMapper.toResponseDTO(user);

        // 5. Retornar respuesta
        return new AuthResponseDTO(
                token,
                null,  // TODO: Implementar refresh token
                userResponseDTO,
                "Bearer"
        );
    }

    /**
     * Generar un token JWT para un usuario
     *
     * @param user Usuario autenticado
     * @return Token JWT
     */
    private String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getCorreo())  // Correo como subject
                .claim("userId", user.getId())
                .claim("nombre", user.getNombre())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validar un token JWT y retornar el correo del usuario
     *
     * @param token Token JWT
     * @return Correo del usuario
     */
    public String validateTokenAndGetEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();  // Retorna el correo (subject)
    }

    /**
     * Obtener la clave de firma para JWT
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
