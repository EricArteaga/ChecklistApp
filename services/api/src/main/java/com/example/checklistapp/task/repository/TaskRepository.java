package com.example.checklistapp.task.repository;

import com.example.checklistapp.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByIdUsuario(Integer idUsuario);
    List<Task> findByIdUsuarioAndCompletada(Integer idUsuario, Boolean completada);
    List<Task> findByIdUsuarioAndFechaProgramacionBetween(Integer idUsuario, LocalDate startDate, LocalDate endDate);
    List<Task> findByIdUsuarioAndFechaRealizacionBetween(Integer idUsuario, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Task t WHERE t.idUsuario = :userId AND t.completada = true " +
           "AND t.fechaRealizacion >= :startDate AND t.fechaRealizacion <= :endDate " +
           "ORDER BY t.fechaRealizacion")
    List<Task> findCompletedTasksByDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Task t WHERE t.idUsuario = :userId AND t.completada = false " +
           "AND t.fechaProgramacion <= :today ORDER BY t.fechaProgramacion")
    List<Task> findPendingTasksUpToDate(@Param("userId") Integer userId, @Param("today") LocalDate today);

    List<Task> findByIdTipo(Integer idTipo);
    long countByIdUsuario(Integer idUsuario);
    long countByIdTipo(Integer idTipo);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.idUsuario = :userId AND t.completada = true " +
           "AND t.fechaRealizacion = :date")
    Long countCompletedTasksByDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "LEFT JOIN FETCH t.subitems " +
           "WHERE t.id = :id")
    Optional<Task> findByIdWithUsuarioAndTipo(@Param("id") Integer id);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "LEFT JOIN FETCH t.subitems " +
           "WHERE t.idUsuario = :idUsuario " +
           "ORDER BY t.fechaCreacion DESC")
    List<Task> findByIdUsuarioWithUsuarioAndTipo(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT t FROM Task t " +
                   "LEFT JOIN FETCH t.usuario " +
                   "LEFT JOIN FETCH t.tipo " +
                   "LEFT JOIN FETCH t.subitems " +
                   "ORDER BY t.fechaCreacion DESC",
           countQuery = "SELECT COUNT(t) FROM Task t")
    Page<Task> findAllWithUsuarioAndTipo(Pageable pageable);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.usuario " +
           "LEFT JOIN FETCH t.tipo " +
           "LEFT JOIN FETCH t.subitems " +
           "WHERE t.idUsuario = :idUsuario AND t.completada = false " +
           "ORDER BY t.fechaProgramacion")
    List<Task> findPendingTasksWithUsuarioAndTipo(@Param("idUsuario") Integer idUsuario);
}
