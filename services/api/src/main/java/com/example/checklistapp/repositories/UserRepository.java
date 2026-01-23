package com.example.checklistapp.repositories;

import com.example.checklistapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // indica que esta interfaz es un repositorio de datos (Spring Data JPA crea la implementación)
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Consulta derivada: busca un usuario por su correo electrónico (usado para autenticación)
    // Optional permite manejar el caso donde no existe el usuario sin lanzar excepciones
    Optional<User> findByCorreo(String correo);
    
    // Consulta derivada: verifica si ya existe un usuario con ese correo (útil para validación en registro)
    boolean existsByCorreo(String correo);
}