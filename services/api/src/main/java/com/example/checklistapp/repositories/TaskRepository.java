package com.example.checklistapp.repositories;

import com.example.checklistapp.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository // indica que esta interfaz es un repositorio de datos (Spring Data JPA crea la implementación automáticamente)
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    // Consulta derivada: encuentra todas las tareas de un usuario específico
    List<Task> findByIdUsuario(Integer idUsuario);
    
    // Consulta derivada: encuentra tareas de un usuario filtrando por estado de completitud
    List<Task> findByIdUsuarioAndCompletada(Integer idUsuario, Boolean completada);
    
    // Consulta derivada: encuentra tareas programadas dentro de un rango de fechas para un usuario
    List<Task> findByIdUsuarioAndFechaProgramacionBetween(
        Integer idUsuario, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    // Consulta derivada: encuentra tareas completadas dentro de un rango de fechas para un usuario
    List<Task> findByIdUsuarioAndFechaRealizacionBetween(
        Integer idUsuario, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    // Consulta JPQL personalizada: obtiene tareas completadas en un rango de fechas, ordenadas por fecha de realización
    @Query("SELECT t FROM Task t WHERE t.idUsuario = :userId AND t.completada = true " +
           "AND t.fechaRealizacion >= :startDate AND t.fechaRealizacion <= :endDate " +
           "ORDER BY t.fechaRealizacion")
    List<Task> findCompletedTasksByDateRange(
        @Param("userId") Integer userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    // Consulta JPQL personalizada: obtiene tareas pendientes hasta una fecha específica, ordenadas por programación
    @Query("SELECT t FROM Task t WHERE t.idUsuario = :userId AND t.completada = false " +
           "AND t.fechaProgramacion <= :today ORDER BY t.fechaProgramacion")
    List<Task> findPendingTasksUpToDate(
        @Param("userId") Integer userId, 
        @Param("today") LocalDate today
    );
    
    // Consulta derivada: encuentra todas las tareas asociadas a un tipo específico
    List<Task> findByIdTipo(Integer idTipo);
    
    // Consulta JPQL personalizada: cuenta cuántas tareas completó un usuario en una fecha específica
    // Útil para generar estadísticas de actividad para el heatmap
    @Query("SELECT COUNT(t) FROM Task t WHERE t.idUsuario = :userId AND t.completada = true " +
           "AND t.fechaRealizacion = :date")
    Long countCompletedTasksByDate(@Param("userId") Integer userId, @Param("date") LocalDate date);
}