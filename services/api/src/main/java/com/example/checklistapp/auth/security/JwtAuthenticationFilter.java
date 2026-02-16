package com.example.checklistapp.auth.security;

import com.example.checklistapp.auth.service.AuthService;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro JWT que intercepta cada request para autenticar usuarios
 *
 * Flujo:
 * 1. Extrae token del header "Authorization: Bearer <token>"
 * 2. Valida token con AuthService
 * 3. Busca usuario en BD
 * 4. Autentica usuario en Spring Security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            AuthService authService,
            UserRepository userRepository
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer header Authorization
        String authorizationHeader = request.getHeader("Authorization");

        // 2. Verificar que tenga el formato "Bearer <token>"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);  // Remover "Bearer "

            try {
                // 3. Validar token y obtener correo
                String correo = authService.validateTokenAndGetEmail(token);

                // 4. Buscar usuario
                User user = userRepository.findByCorreo(correo).orElse(null);

                if (user != null) {
                    // 5. Crear autenticación Spring Security
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.emptyList()  // TODO: Agregar roles/authorities
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Establecer autenticación en contexto
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token inválido → No autenticar (continuará sin autenticación)
                logger.error("Error al validar token JWT: " + e.getMessage());
            }
        }

        // 7. Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}
