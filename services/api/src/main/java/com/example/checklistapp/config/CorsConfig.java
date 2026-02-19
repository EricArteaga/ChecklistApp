package com.example.checklistapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing)
 *
 * Permite que el frontend (React en puerto 3000) pueda hacer
 * peticiones al backend (Spring Boot en puerto 8080)
 */
@Configuration
public class CorsConfig {

    /**
     * Filtro CORS que permite requests desde el frontend
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (frontend en desarrollo)
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:3001"); // Por si craco usa otro puerto

        // Credenciales permitidas (cookies, headers de autorización)
        config.setAllowCredentials(true);

        // Headers permitidos
        config.addAllowedHeader("*");

        // Métodos HTTP permitidos
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Aplicar a todos los endpoints de la API
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
