package com.example.checklistapp.repositories;

import com.example.checklistapp.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    // === QUERIES CON JOIN FETCH (para DTOs) ===

    /**
     * Trae Task con Usuario y Tipo en una sola query (evita N+1).
     *
     * <p><b>JOIN FETCH:</b>
     * <ul>
     *   <li>LEFT JOIN FETCH t.tipo → permite que tipo sea NULL</li>
     *   <li>No necesitamos LEFT JOIN FETCH t.usuario porque usuario es NOT NULL</li>
     * </ul>
     *
     * <p>Esta query es equivalente a:
     * <pre>
     * SELECT t.*, u.*, ty.*
     * FROM tareas t
     * JOIN usuarios u ON t.id_usuario = u.id
     * LEFT JOIN tipos ty ON t.id_tipo = ty.id
     * WHERE t.id = ?
     * </pre>
     *
     * @param id ID de la tarea
     * @return Optional con Task y relaciones cargadas
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "WHERE t.id = :id")
    Optional<Task> findByIdWithUsuarioAndTipo(@Param("id") Integer id);

    /**
     * Busca todas las tareas de un usuario con JOIN FETCH.
     *
     * <p>Útil para listados por usuario con nombre de tipo incluido.
     *
     * @param idUsuario ID del usuario
     * @return Lista de tareas con relaciones cargadas
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "WHERE t.idUsuario = :idUsuario " +
           "ORDER BY t.fechaCreacion DESC")
    List<Task> findByIdUsuarioWithUsuarioAndTipo(@Param("idUsuario") Integer idUsuario);

    /**
     * Para listados paginados con JOIN FETCH.
     *
     * <p><b>NOTA:</b> Page{@literal <Task>} + JOIN FETCH requiere un CountQuery separado
     * para evitar HibernateException: "cannot fetch multiple bags".
     *
     * <p>Alternativa: usar two-pass query (primero IDs, luego entidades).
     *
     * @param pageable Paginación y ordenamiento
     * @return Página de tareas con tipo cargado
     */
    @Query(value = "SELECT t FROM Task t " +
                   "JOIN FETCH t.usuario " +
                   "LEFT JOIN FETCH t.tipo " +
                   "ORDER BY t.fechaCreacion DESC",
           countQuery = "SELECT COUNT(t) FROM Task t")
    Page<Task> findAllWithUsuarioAndTipo(Pageable pageable);

    /**
     * Busca tareas pendientes de un usuario con JOIN FETCH.
     *
     * @param idUsuario ID del usuario
     * @return Lista de tareas pendientes ordenadas por fecha de programación
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "WHERE t.idUsuario = :idUsuario AND t.completada = false " +
           "ORDER BY t.fechaProgramacion")
    List<Task> findPendingTasksWithUsuarioAndTipo(@Param("idUsuario") Integer idUsuario);
}