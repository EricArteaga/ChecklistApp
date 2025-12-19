package com.example.checklistapp; // paquete base de la aplicación backend

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // activa la configuración automática de Spring Boot
public class Application {
    public static void main(String[] args) {
        // Punto de entrada de la aplicación Spring Boot: arranca el contexto y servidor embebido
        SpringApplication.run(Application.class, args);
    }
}
