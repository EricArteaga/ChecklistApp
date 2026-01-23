package com.example.checklistapp.repositories;

import com.example.checklistapp.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // indica que esta interfaz es un repositorio de datos (Spring Data JPA crea la implementación)
public interface TypeRepository extends JpaRepository<Type, Integer> {
    
    // Consulta derivada: encuentra todos los tipos/categorías de un usuario específico
    List<Type> findByIdUsuario(Integer idUsuario);
    
    // Consulta derivada: busca un tipo específico de un usuario por su nombre (envuelto en Optional para manejar ausencia)
    Optional<Type> findByIdUsuarioAndNombre(Integer idUsuario, String nombre);
    
    // Consulta derivada: verifica si existe un tipo con ese nombre para un usuario (útil para evitar duplicados)
    boolean existsByIdUsuarioAndNombre(Integer idUsuario, String nombre);
    
    // Consulta derivada: busca tipos cuyo nombre contenga la cadena especificada (case-insensitive)
    // Útil para implementar búsqueda con autocompletado en la UI
    List<Type> findByIdUsuarioAndNombreContainingIgnoreCase(Integer idUsuario, String nombre);
}