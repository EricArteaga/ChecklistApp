package com.example.checklistapp.task.repository;

import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@DataJpaTest**: Configura solo lo necesario para probar repositorios JPA
 *    - Carga EntityManager, repositories, y configura BD embebida (H2)
 *    - NO carga controllers, services, ni el resto de la aplicación
 * 2. **TestEntityManager**: Permite persistir entidades para los tests
 *    - Es una versión especial de EntityManager para testing
 * 3. **@ActiveProfiles("test")**: Activa perfil de test (usa application-test.yml)
 *
 * ESTRATEGIA DE TESTING:
 * ======================
 * - Persistimos entidades reales en BD H2
 * - Probamos consultas JPA (@Query) y métodos derivados
 * - Verificamos que se carguen relaciones (JOIN FETCH)
 * - Tests independientes (la BD se limpia entre tests)
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    // === FIXTURE: Datos de prueba ===
    private User testUser;
    private Type testType;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Crear y persistir usuario
        testUser = new User();
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");
        testUser.setHashContrasena("hash123");
        entityManager.persist(testUser);
        entityManager.flush();

        // Crear y persistir tipo
        testType = new Type();
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");
        testType.setIdUsuario(testUser.getId());
        entityManager.persist(testType);
        entityManager.flush();

        // Crear tarea de prueba (sin persistir aún)
        testTask = new Task();
        testTask.setNombre("Tarea de prueba");
        testTask.setDescripcion("Descripción de prueba");
        testTask.setIdUsuario(testUser.getId());
        testTask.setIdTipo(testType.getId());
        testTask.setCompletada(false);
        testTask.setFechaProgramacion(LocalDate.now().plusDays(7));
    }

    // ========================================
    // TESTS FIND BY ID WITH RELATIONS
    // ========================================

    @Test
    @DisplayName("✅ findByIdWithUsuarioAndTipo - Existing task returns task with relations loaded")
    void findByIdWithUsuarioAndTipo_ExistingTask_ReturnsTaskWithRelations() {
        // GIVEN: Persistir tarea
        entityManager.persist(testTask);
        entityManager.flush();
        entityManager.clear(); // Limpiar cache para forzar reload desde BD

        // WHEN: Ejecutar query con JOIN FETCH
        Optional<Task> result = taskRepository.findByIdWithUsuarioAndTipo(testTask.getId());

        // THEN: Verificar que se cargó tarea con relaciones
        assertThat(result).isPresent();
        Task found = result.get();
        assertThat(found.getNombre()).isEqualTo("Tarea de prueba");

        // Verificar que relaciones están cargadas (sin LazyInitializationException)
        assertThat(found.getUsuario()).isNotNull();
        assertThat(found.getUsuario().getNombre()).isEqualTo("Usuario Test");
        assertThat(found.getTipo()).isNotNull();
        assertThat(found.getTipo().getNombre()).isEqualTo("Trabajo");

        // Verificar colección de subitems (vacía pero cargada)
        assertThat(found.getSubitems()).isNotNull();
        assertThat(found.getSubitems()).isEmpty();
    }

    @Test
    @DisplayName("❌ findByIdWithUsuarioAndTipo - Non-existent task returns empty")
    void findByIdWithUsuarioAndTipo_NonExistentTask_ReturnsEmpty() {
        // WHEN: Buscar tarea inexistente
        Optional<Task> result = taskRepository.findByIdWithUsuarioAndTipo(999);

        // THEN: Retornar empty
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ findByIdWithUsuarioAndTipo - Task with subitems loads all relations")
    void findByIdWithUsuarioAndTipo_TaskWithSubitems_LoadsAllRelations() {
        // GIVEN: Tarea con subitems
        entityManager.persist(testTask);
        entityManager.flush();

        Subitem subitem1 = new Subitem();
        subitem1.setTarea(testTask);
        subitem1.setDescription("Subitem 1");
        subitem1.setChecked(false);
        entityManager.persist(subitem1);

        Subitem subitem2 = new Subitem();
        subitem2.setTarea(testTask);
        subitem2.setDescription("Subitem 2");
        subitem2.setChecked(true);
        entityManager.persist(subitem2);

        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar tarea con JOIN FETCH
        Optional<Task> result = taskRepository.findByIdWithUsuarioAndTipo(testTask.getId());

        // THEN: Verificar que se cargaron subitems
        assertThat(result).isPresent();
        Task found = result.get();
        assertThat(found.getSubitems()).hasSize(2);
        assertThat(found.getSubitems().get(0).getDescription()).isEqualTo("Subitem 1");
        assertThat(found.getSubitems().get(1).getDescription()).isEqualTo("Subitem 2");
    }

    // ========================================
    // TESTS FIND BY USUARIO WITH RELATIONS
    // ========================================

    @Test
    @DisplayName("✅ findByIdUsuarioWithUsuarioAndTipo - Returns tasks ordered by creation date")
    void findByIdUsuarioWithUsuarioAndTipo_ExistingUser_ReturnsTasksOrderedByCreationDate() {
        // GIVEN: Usuario con múltiples tareas
        Task task1 = createTask("Tarea 1", LocalDate.now().plusDays(1));
        Task task2 = createTask("Tarea 2", LocalDate.now().plusDays(2));
        Task task3 = createTask("Tarea 3", LocalDate.now().plusDays(3));

        entityManager.persist(task1);
        entityManager.flush(); // Forzar generación de fechaCreación
        entityManager.persist(task2);
        entityManager.flush();
        entityManager.persist(task3);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar tareas por usuario
        List<Task> tasks = taskRepository.findByIdUsuarioWithUsuarioAndTipo(testUser.getId());

        // THEN: Retornar tareas ordenadas por fechaCreacion DESC (más recientes primero)
        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getNombre()).isEqualTo("Tarea 3"); // Última creada
        assertThat(tasks.get(1).getNombre()).isEqualTo("Tarea 2");
        assertThat(tasks.get(2).getNombre()).isEqualTo("Tarea 1");

        // Verificar que relaciones están cargadas
        assertThat(tasks.get(0).getUsuario()).isNotNull();
        assertThat(tasks.get(0).getTipo()).isNotNull();
    }

    @Test
    @DisplayName("❌ findByIdUsuarioWithUsuarioAndTipo - Non-existent user returns empty list")
    void findByIdUsuarioWithUsuarioAndTipo_NonExistentUser_ReturnsEmptyList() {
        // WHEN: Buscar tareas de usuario inexistente
        List<Task> tasks = taskRepository.findByIdUsuarioWithUsuarioAndTipo(999);

        // THEN: Retornar lista vacía
        assertThat(tasks).isEmpty();
    }

    // ========================================
    // TESTS FIND ALL WITH PAGINATION
    // ========================================

    @Test
    @DisplayName("✅ findAllWithUsuarioAndTipo - Returns paginated results")
    void findAllWithUsuarioAndTipo_MultipleTasks_ReturnsPageWithRelations() {
        // GIVEN: Varias tareas en BD
        for (int i = 1; i <= 5; i++) {
            Task task = createTask("Tarea " + i, LocalDate.now().plusDays(i));
            entityManager.persist(task);
        }
        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar con paginación (pagina 0, tamaño 3)
        Page<Task> page = taskRepository.findAllWithUsuarioAndTipo(PageRequest.of(0, 3));

        // THEN: Retornar primera página con 3 elementos
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2); // 5 elementos / 3 por página = 2 páginas

        // Verificar que relaciones están cargadas
        Task firstTask = page.getContent().get(0);
        assertThat(firstTask.getUsuario()).isNotNull();
        assertThat(firstTask.getTipo()).isNotNull();
    }

    @Test
    @DisplayName("✅ findAllWithUsuarioAndTipo - Empty database returns empty page")
    void findAllWithUsuarioAndTipo_NoTasks_ReturnsEmptyPage() {
        // WHEN: No hay tareas en BD
        Page<Task> page = taskRepository.findAllWithUsuarioAndTipo(PageRequest.of(0, 10));

        // THEN: Retornar página vacía
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    // ========================================
    // TESTS FIND PENDING TASKS
    // ========================================

    @Test
    @DisplayName("✅ findPendingTasksWithUsuarioAndTipo - Returns only pending tasks ordered by date")
    void findPendingTasksWithUsuarioAndTipo_MixedTasks_ReturnsOnlyPendingTasks() {
        // GIVEN: Tareas pendientes y completadas
        Task pending1 = createTask("Pendiente 1", LocalDate.now().plusDays(5));
        pending1.setCompletada(false);
        entityManager.persist(pending1);

        Task completed = createTask("Completada", LocalDate.now().plusDays(3));
        completed.setCompletada(true);
        entityManager.persist(completed);

        Task pending2 = createTask("Pendiente 2", LocalDate.now().plusDays(1));
        pending2.setCompletada(false);
        entityManager.persist(pending2);

        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar tareas pendientes
        List<Task> pendingTasks = taskRepository.findPendingTasksWithUsuarioAndTipo(testUser.getId());

        // THEN: Retornar solo pendientes, ordenadas por fechaProgramacion
        assertThat(pendingTasks).hasSize(2);
        assertThat(pendingTasks.get(0).getNombre()).isEqualTo("Pendiente 2"); // Fecha más cercana
        assertThat(pendingTasks.get(1).getNombre()).isEqualTo("Pendiente 1");
        assertThat(pendingTasks).allMatch(task -> !task.getCompletada());
    }

    @Test
    @DisplayName("❌ findPendingTasksWithUsuarioAndTipo - All tasks completed returns empty list")
    void findPendingTasksWithUsuarioAndTipo_AllTasksCompleted_ReturnsEmptyList() {
        // GIVEN: Todas las tareas completadas
        Task task = createTask("Completada", LocalDate.now());
        task.setCompletada(true);
        entityManager.persist(task);
        entityManager.flush();

        // WHEN: Buscar tareas pendientes
        List<Task> pendingTasks = taskRepository.findPendingTasksWithUsuarioAndTipo(testUser.getId());

        // THEN: Retornar lista vacía
        assertThat(pendingTasks).isEmpty();
    }

    // ========================================
    // TESTS COUNT BY USUARIO
    // ========================================

    @Test
    @DisplayName("✅ countByIdUsuario - Returns correct count")
    void countByIdUsuario_ExistingUser_ReturnsCorrectCount() {
        // GIVEN: Usuario con 3 tareas
        for (int i = 0; i < 3; i++) {
            Task task = createTask("Tarea " + i, LocalDate.now());
            entityManager.persist(task);
        }
        entityManager.flush();

        // WHEN: Contar tareas por usuario
        long count = taskRepository.countByIdUsuario(testUser.getId());

        // THEN: Retornar 3
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ countByIdUsuario - User with no tasks returns zero")
    void countByIdUsuario_UserWithNoTasks_ReturnsZero() {
        // GIVEN: Usuario sin tareas

        // WHEN: Contar tareas
        long count = taskRepository.countByIdUsuario(testUser.getId());

        // THEN: Retornar 0
        assertThat(count).isZero();
    }

    // ========================================
    // TESTS COUNT BY TIPO
    // ========================================

    @Test
    @DisplayName("✅ countByIdTipo - Returns correct count")
    void countByIdTipo_ExistingType_ReturnsCorrectCount() {
        // GIVEN: 2 tareas del mismo tipo
        Task task1 = createTask("Tarea 1", LocalDate.now());
        Task task2 = createTask("Tarea 2", LocalDate.now());
        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        // WHEN: Contar tareas por tipo
        long count = taskRepository.countByIdTipo(testType.getId());

        // THEN: Retornar 2
        assertThat(count).isEqualTo(2);
    }

    // ========================================
    // TESTS FIND BY ID USUARIO
    // ========================================

    @Test
    @DisplayName("✅ findByIdUsuario - Returns all user tasks without relations")
    void findByIdUsuario_ExistingUser_ReturnsTasksWithoutRelations() {
        // GIVEN: Usuario con tareas
        Task task = createTask("Tarea", LocalDate.now());
        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar tareas por usuario (método derivado)
        List<Task> tasks = taskRepository.findByIdUsuario(testUser.getId());

        // THEN: Retornar tareas
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getNombre()).isEqualTo("Tarea");
    }

    // ========================================
    // TESTS FIND BY ID USUARIO AND COMPLETADA
    // ========================================

    @Test
    @DisplayName("✅ findByIdUsuarioAndCompletada - Returns tasks filtered by completion status")
    void findByIdUsuarioAndCompletada_MixedTasks_ReturnsFilteredTasks() {
        // GIVEN: Tareas con diferentes estados
        Task pending = createTask("Pendiente", LocalDate.now());
        pending.setCompletada(false);
        entityManager.persist(pending);

        Task completed = createTask("Completada", LocalDate.now());
        completed.setCompletada(true);
        entityManager.persist(completed);

        entityManager.flush();

        // WHEN: Buscar tareas completadas
        List<Task> completedTasks = taskRepository.findByIdUsuarioAndCompletada(testUser.getId(), true);

        // THEN: Retornar solo completadas
        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getNombre()).isEqualTo("Completada");

        // WHEN: Buscar tareas pendientes
        List<Task> pendingTasks = taskRepository.findByIdUsuarioAndCompletada(testUser.getId(), false);

        // THEN: Retornar solo pendientes
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getNombre()).isEqualTo("Pendiente");
    }

    // ========================================
    // TESTS COUNT COMPLETED TASKS BY DATE
    // ========================================

    @Test
    @DisplayName("✅ countCompletedTasksByDate - Returns count of tasks completed on specific date")
    void countCompletedTasksByDate_CompletedTasks_ReturnsCorrectCount() {
        // GIVEN: Tareas completadas en diferentes fechas
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Task task1 = createTask("Tarea 1", today);
        task1.setCompletada(true);
        task1.setFechaRealizacion(today);
        entityManager.persist(task1);

        Task task2 = createTask("Tarea 2", today);
        task2.setCompletada(true);
        task2.setFechaRealizacion(today);
        entityManager.persist(task2);

        Task task3 = createTask("Tarea 3", today);
        task3.setCompletada(true);
        task3.setFechaRealizacion(yesterday);
        entityManager.persist(task3);

        entityManager.flush();

        // WHEN: Contar tareas completadas hoy
        Long count = taskRepository.countCompletedTasksByDate(testUser.getId(), today);

        // THEN: Retornar 2 (solo las de hoy)
        assertThat(count).isEqualTo(2);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private Task createTask(String nombre, LocalDate fechaProgramacion) {
        Task task = new Task();
        task.setNombre(nombre);
        task.setDescripcion("Descripción de " + nombre);
        task.setIdUsuario(testUser.getId());
        task.setIdTipo(testType.getId());
        task.setCompletada(false);
        task.setFechaProgramacion(fechaProgramacion);
        return task;
    }
}
