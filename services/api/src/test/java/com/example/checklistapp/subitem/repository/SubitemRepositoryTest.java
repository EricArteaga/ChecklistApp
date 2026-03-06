package com.example.checklistapp.subitem.repository;

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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubitemRepository
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@DataJpaTest**: Configura BD H2 y repositorios para testing
 * 2. **TestEntityManager**: Permite persistir entidades en la BD de test
 * 3. **@Query personalizada**: findByIdTarea, deleteByIdTarea
 * 4. **@Modifying**: Para queries de DELETE/UPDATE
 *
 * QUÉ PROBAMOS:
 * =============
 * - Consultas por tarea (findByIdTarea)
 * - Eliminación por tarea (deleteByIdTarea)
 * - Relación ManyToOne con Task
 * - Operaciones CRUD básicas
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SubitemRepository Integration Tests")
class SubitemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubitemRepository subitemRepository;

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

        // Crear y persistir tarea
        testTask = new Task();
        testTask.setNombre("Tarea de prueba");
        testTask.setDescripcion("Descripción de prueba");
        testTask.setIdUsuario(testUser.getId());
        testTask.setIdTipo(testType.getId());
        testTask.setCompletada(false);
        testTask.setFechaProgramacion(java.time.LocalDate.now().plusDays(7));
        entityManager.persist(testTask);
        entityManager.flush();
    }

    // ========================================
    // TESTS FIND BY ID TAREA
    // ========================================

    @Test
    @DisplayName("✅ findByIdTarea - Returns all subitems for task")
    void findByIdTarea_ExistingTask_ReturnsAllSubitems() {
        // GIVEN: Tarea con múltiples subitems
        Subitem subitem1 = createSubitem("Subitem 1", false);
        Subitem subitem2 = createSubitem("Subitem 2", true);
        Subitem subitem3 = createSubitem("Subitem 3", false);

        entityManager.persist(subitem1);
        entityManager.persist(subitem2);
        entityManager.persist(subitem3);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar subitems por tarea
        List<Subitem> subitems = subitemRepository.findByIdTarea(testTask.getId());

        // THEN: Retornar todos los subitems
        assertThat(subitems).hasSize(3);
        assertThat(subitems).extracting(Subitem::getDescription)
                .containsExactlyInAnyOrder("Subitem 1", "Subitem 2", "Subitem 3");
    }

    @Test
    @DisplayName("❌ findByIdTarea - Non-existent task returns empty list")
    void findByIdTarea_NonExistentTask_ReturnsEmptyList() {
        // GIVEN: No persistir subitems

        // WHEN: Buscar subitems de tarea inexistente
        List<Subitem> subitems = subitemRepository.findByIdTarea(999);

        // THEN: Retornar lista vacía
        assertThat(subitems).isEmpty();
    }

    @Test
    @DisplayName("✅ findByIdTarea - Task with no subitems returns empty list")
    void findByIdTarea_TaskWithNoSubitems_ReturnsEmptyList() {
        // GIVEN: Tarea sin subitems

        // WHEN: Buscar subitems
        List<Subitem> subitems = subitemRepository.findByIdTarea(testTask.getId());

        // THEN: Retornar lista vacía
        assertThat(subitems).isEmpty();
    }

    @Test
    @DisplayName("✅ findByIdTarea - Returns only subitems for specific task")
    void findByIdTarea_MultipleTasks_ReturnsOnlyTaskSubitems() {
        // GIVEN: Crear otra tarea con sus subitems
        Task task2 = new Task();
        task2.setNombre("Tarea 2");
        task2.setDescripcion("Descripción");
        task2.setIdUsuario(testUser.getId());
        task2.setIdTipo(testType.getId());
        task2.setCompletada(false);
        task2.setFechaProgramacion(java.time.LocalDate.now().plusDays(5));
        entityManager.persist(task2);
        entityManager.flush();

        // Subitems de la primera tarea
        Subitem subitem1 = createSubitem("Subitem tarea 1", false);
        entityManager.persist(subitem1);

        // Subitems de la segunda tarea
        Subitem subitem2 = new Subitem();
        subitem2.setTarea(task2);
        subitem2.setDescription("Subitem tarea 2");
        subitem2.setChecked(false);
        entityManager.persist(subitem2);

        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar subitems de la primera tarea
        List<Subitem> task1Subitems = subitemRepository.findByIdTarea(testTask.getId());

        // THEN: Retornar solo subitems de la primera tarea
        assertThat(task1Subitems).hasSize(1);
        assertThat(task1Subitems.get(0).getDescription()).isEqualTo("Subitem tarea 1");
        assertThat(task1Subitems.get(0).getTarea().getId()).isEqualTo(testTask.getId());
    }

    // ========================================
    // TESTS DELETE BY ID TAREA
    // ========================================

    @Test
    @DisplayName("✅ deleteByIdTarea - Deletes all subitems for task")
    void deleteByIdTarea_ExistingTask_DeletesAllSubitems() {
        // GIVEN: Tarea con múltiples subitems
        Subitem subitem1 = createSubitem("Subitem 1", false);
        Subitem subitem2 = createSubitem("Subitem 2", true);
        Subitem subitem3 = createSubitem("Subitem 3", false);

        entityManager.persist(subitem1);
        entityManager.persist(subitem2);
        entityManager.persist(subitem3);
        entityManager.flush();

        // Verificar que hay 3 subitems
        List<Subitem> beforeDelete = subitemRepository.findByIdTarea(testTask.getId());
        assertThat(beforeDelete).hasSize(3);

        // WHEN: Eliminar todos los subitems de la tarea
        subitemRepository.deleteByIdTarea(testTask.getId());
        entityManager.flush();

        // THEN: Todos los subitems eliminados
        List<Subitem> afterDelete = subitemRepository.findByIdTarea(testTask.getId());
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @DisplayName("✅ deleteByIdTarea - Non-existent task does not throw error")
    void deleteByIdTarea_NonExistentTask_DoesNotThrowError() {
        // GIVEN: No hay subitems

        // WHEN: Intentar eliminar subitems de tarea inexistente
        // THEN: No debe lanzar excepción - simplemente ejecutar
        subitemRepository.deleteByIdTarea(999);
        entityManager.flush();
        // Si llegamos aquí, todo está bien ✅
    }

    @Test
    @DisplayName("✅ deleteByIdTarea - Task with no subitems executes successfully")
    void deleteByIdTarea_TaskWithNoSubitems_ExecutesSuccessfully() {
        // GIVEN: Tarea sin subitems

        // WHEN: Eliminar subitems (no hay ninguno)
        subitemRepository.deleteByIdTarea(testTask.getId());
        entityManager.flush();

        // THEN: No hay cambios (sigue sin subitems)
        List<Subitem> subitems = subitemRepository.findByIdTarea(testTask.getId());
        assertThat(subitems).isEmpty();
    }

    // ========================================
    // TESTS SAVE (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ save - Creates subitem with generated ID and timestamp")
    void save_ValidSubitem_CreatesSubitemWithIdAndTimestamp() {
        // GIVEN: Subitem sin ID
        Subitem subitem = createSubitem("Nuevo subitem", false);
        assertThat(subitem.getId()).isNull();
        assertThat(subitem.getFechaCreacion()).isNull();

        // WHEN: Guardar subitem
        Subitem saved = subitemRepository.save(subitem);

        // THEN: Asignar ID y fechaCreación
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFechaCreacion()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Nuevo subitem");
        assertThat(saved.getChecked()).isFalse();
    }

    @Test
    @DisplayName("✅ save - Multiple subitems auto-increment IDs")
    void save_MultipleSubitems_AutoIncrementIds() {
        // GIVEN: Crear múltiples subitems
        Subitem subitem1 = createSubitem("Subitem 1", false);
        Subitem subitem2 = createSubitem("Subitem 2", true);

        // WHEN: Persistir ambos
        subitemRepository.save(subitem1);
        subitemRepository.save(subitem2);
        entityManager.flush();

        // THEN: Tener IDs consecutivos
        assertThat(subitem1.getId()).isNotNull();
        assertThat(subitem2.getId()).isNotNull();
        assertThat(subitem2.getId()).isEqualTo(subitem1.getId() + 1);
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ findById - Existing subitem returns subitem")
    void findById_ExistingSubitem_ReturnsSubitem() {
        // GIVEN: Persistir subitem
        Subitem subitem = createSubitem("Subitem de prueba", true);
        entityManager.persist(subitem);
        entityManager.flush();

        // WHEN: Buscar por ID
        var result = subitemRepository.findById(subitem.getId());

        // THEN: Retornar subitem
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Subitem de prueba");
        assertThat(result.get().getChecked()).isTrue();
        assertThat(result.get().getTarea()).isNotNull();
    }

    @Test
    @DisplayName("❌ findById - Non-existent ID returns empty")
    void findById_NonExistentId_ReturnsEmpty() {
        // WHEN: Buscar ID inexistente
        var result = subitemRepository.findById(999);

        // THEN: Retornar empty
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ save - Updates existing subitem")
    void save_ExistingSubitem_UpdatesSubitem() {
        // GIVEN: Persistir subitem
        Subitem subitem = createSubitem("Subitem original", false);
        entityManager.persist(subitem);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Actualizar descripción y estado
        Subitem found = subitemRepository.findById(subitem.getId()).orElseThrow();
        found.setDescription("Subitem actualizado");
        found.setChecked(true);
        subitemRepository.save(found);
        entityManager.flush();
        entityManager.clear();

        // THEN: Cambios persistidos
        Subitem updated = subitemRepository.findById(subitem.getId()).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("Subitem actualizado");
        assertThat(updated.getChecked()).isTrue();
        assertThat(updated.getTarea().getId()).isEqualTo(testTask.getId()); // No cambió
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ deleteById - Deletes existing subitem")
    void deleteById_ExistingSubitem_DeletesSubitem() {
        // GIVEN: Persistir subitem
        Subitem subitem = createSubitem("Subitem a eliminar", false);
        entityManager.persist(subitem);
        entityManager.flush();
        Integer subitemId = subitem.getId();

        // WHEN: Eliminar subitem
        subitemRepository.deleteById(subitemId);
        entityManager.flush();

        // THEN: Subitem eliminado
        var result = subitemRepository.findById(subitemId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ delete - Deletes existing subitem entity")
    void delete_ExistingSubitem_DeletesSubitem() {
        // GIVEN: Persistir subitem
        Subitem subitem = createSubitem("Subitem a eliminar", false);
        entityManager.persist(subitem);
        entityManager.flush();

        // WHEN: Eliminar entidad
        Subitem found = subitemRepository.findById(subitem.getId()).orElseThrow();
        subitemRepository.delete(found);
        entityManager.flush();

        // THEN: Subitem eliminado
        var result = subitemRepository.findById(subitem.getId());
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS COUNT
    // ========================================

    @Test
    @DisplayName("✅ count - Returns correct count")
    void count_MultipleSubitems_ReturnsCorrectCount() {
        // GIVEN: Persistir 3 subitems
        for (int i = 1; i <= 3; i++) {
            Subitem subitem = createSubitem("Subitem " + i, i % 2 == 0);
            entityManager.persist(subitem);
        }
        entityManager.flush();

        // WHEN: Contar subitems
        long count = subitemRepository.count();

        // THEN: Retornar 3
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ count - Empty database returns zero")
    void count_NoSubitems_ReturnsZero() {
        // WHEN: No hay subitems
        long count = subitemRepository.count();

        // THEN: Retornar 0
        assertThat(count).isZero();
    }

    // ========================================
    // TESTS CHECKED/UNCHECKED
    // ========================================

    @Test
    @DisplayName("✅ findByIdTarea - Returns subitems with correct checked status")
    void findByIdTarea_MixedCheckedStatus_ReturnsCorrectStatus() {
        // GIVEN: Subitems con diferentes estados
        Subitem checked1 = createSubitem("Completado 1", true);
        Subitem unchecked = createSubitem("Pendiente", false);
        Subitem checked2 = createSubitem("Completado 2", true);

        entityManager.persist(checked1);
        entityManager.persist(unchecked);
        entityManager.persist(checked2);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Buscar subitems
        List<Subitem> subitems = subitemRepository.findByIdTarea(testTask.getId());

        // THEN: Retornar con estados correctos
        assertThat(subitems).hasSize(3);
        assertThat(subitems).anyMatch(s -> s.getDescription().equals("Completado 1") && s.getChecked());
        assertThat(subitems).anyMatch(s -> s.getDescription().equals("Pendiente") && !s.getChecked());
        assertThat(subitems).anyMatch(s -> s.getDescription().equals("Completado 2") && s.getChecked());
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private Subitem createSubitem(String description, boolean checked) {
        Subitem subitem = new Subitem();
        subitem.setTarea(testTask);
        subitem.setDescription(description);
        subitem.setChecked(checked);
        return subitem;
    }
}
