package com.example.checklistapp.user.repository;

import com.example.checklistapp.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@DataJpaTest**: Configura BD H2 y repositorios para testing
 * 2. **TestEntityManager**: Permite persistir entidades en la BD de test
 * 3. ** Métodos derivados**: findByCorreo, existsByCorreo son generados por Spring Data JPA
 *
 * QUÉ PROBAMOS:
 * =============
 * - Consultas personalizadas (findByCorreo)
 * - Verificación de existencia (existsByCorreo)
 * - Operaciones CRUD básicas
 * - Unique constraints (correo único)
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = new User();
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");
        testUser.setHashContrasena("hash123");
    }

    // ========================================
    // TESTS FIND BY CORREO
    // ========================================

    @Test
    @DisplayName("✅ findByCorreo - Existing user returns user")
    void findByCorreo_ExistingUser_ReturnsUser() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();

        // WHEN: Buscar por correo
        Optional<User> result = userRepository.findByCorreo("test@example.com");

        // THEN: Retornar usuario
        assertThat(result).isPresent();
        User found = result.get();
        assertThat(found.getCorreo()).isEqualTo("test@example.com");
        assertThat(found.getNombre()).isEqualTo("Usuario Test");
        assertThat(found.getHashContrasena()).isEqualTo("hash123");
        assertThat(found.getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("❌ findByCorreo - Non-existent email returns empty")
    void findByCorreo_NonExistentEmail_ReturnsEmpty() {
        // GIVEN: No persistir usuario

        // WHEN: Buscar correo inexistente
        Optional<User> result = userRepository.findByCorreo("nonexistent@example.com");

        // THEN: Retornar empty
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ findByCorreo - Case sensitive search")
    void findByCorreo_DifferentCase_ReturnsEmpty() {
        // GIVEN: Persistir usuario con correo en minúsculas
        entityManager.persist(testUser);
        entityManager.flush();

        // WHEN: Buscar con mayúsculas (case sensitive)
        Optional<User> result = userRepository.findByCorreo("TEST@EXAMPLE.COM");

        // THEN: No encontrar (case sensitive)
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS EXISTS BY CORREO
    // ========================================

    @Test
    @DisplayName("✅ existsByCorreo - Existing email returns true")
    void existsByCorreo_ExistingEmail_ReturnsTrue() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();

        // WHEN: Verificar si existe correo
        boolean exists = userRepository.existsByCorreo("test@example.com");

        // THEN: Retornar true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("❌ existsByCorreo - Non-existent email returns false")
    void existsByCorreo_NonExistentEmail_ReturnsFalse() {
        // GIVEN: No persistir usuario

        // WHEN: Verificar si existe correo
        boolean exists = userRepository.existsByCorreo("nonexistent@example.com");

        // THEN: Retornar false
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("✅ existsByCorreo - After deleting user returns false")
    void existsByCorreo_AfterDeletingUser_ReturnsFalse() {
        // GIVEN: Persistir y luego eliminar usuario
        entityManager.persist(testUser);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findByCorreo("test@example.com").orElseThrow();
        entityManager.remove(found);
        entityManager.flush();

        // WHEN: Verificar si existe
        boolean exists = userRepository.existsByCorreo("test@example.com");

        // THEN: Retornar false (fue eliminado)
        assertThat(exists).isFalse();
    }

    // ========================================
    // TESTS SAVE (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ save - Creates user with generated ID and timestamp")
    void save_ValidUser_CreatesUserWithIdAndTimestamp() {
        // GIVEN: Usuario sin ID
        assertThat(testUser.getId()).isNull();

        // WHEN: Guardar usuario
        User saved = userRepository.save(testUser);

        // THEN: Asignar ID y fechaCreación
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFechaCreacion()).isNotNull();
        assertThat(saved.getCorreo()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("✅ save - Multiple users auto-increment IDs")
    void save_MultipleUsers_AutoIncrementIds() {
        // GIVEN: Crear múltiples usuarios
        User user1 = new User();
        user1.setCorreo("user1@example.com");
        user1.setNombre("User 1");
        user1.setHashContrasena("hash1");

        User user2 = new User();
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");
        user2.setHashContrasena("hash2");

        // WHEN: Persistir ambos
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // THEN: Tener IDs consecutivos
        assertThat(user1.getId()).isNotNull();
        assertThat(user2.getId()).isNotNull();
        assertThat(user2.getId()).isEqualTo(user1.getId() + 1);
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ findById - Existing user returns user")
    void findById_ExistingUser_ReturnsUser() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();

        // WHEN: Buscar por ID
        Optional<User> result = userRepository.findById(testUser.getId());

        // THEN: Retornar usuario
        assertThat(result).isPresent();
        assertThat(result.get().getCorreo()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("❌ findById - Non-existent ID returns empty")
    void findById_NonExistentId_ReturnsEmpty() {
        // WHEN: Buscar ID inexistente
        Optional<User> result = userRepository.findById(999);

        // THEN: Retornar empty
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS FIND ALL
    // ========================================

    @Test
    @DisplayName("✅ findAll - Returns all users")
    void findAll_MultipleUsers_ReturnsAllUsers() {
        // GIVEN: Crear múltiples usuarios
        User user1 = new User();
        user1.setCorreo("user1@example.com");
        user1.setNombre("User 1");
        user1.setHashContrasena("hash1");

        User user2 = new User();
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");
        user2.setHashContrasena("hash2");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // WHEN: Buscar todos
        List<User> users = userRepository.findAll();

        // THEN: Retornar todos los usuarios
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getCorreo)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("✅ findAll - Empty database returns empty list")
    void findAll_NoUsers_ReturnsEmptyList() {
        // WHEN: No hay usuarios en BD
        List<User> users = userRepository.findAll();

        // THEN: Retornar lista vacía
        assertThat(users).isEmpty();
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ save - Updates existing user")
    void save_ExistingUser_UpdatesUser() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Actualizar nombre
        User found = userRepository.findById(testUser.getId()).orElseThrow();
        found.setNombre("Usuario Actualizado");
        userRepository.save(found);
        entityManager.flush();
        entityManager.clear();

        // THEN: Cambios persistidos
        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getNombre()).isEqualTo("Usuario Actualizado");
        assertThat(updated.getCorreo()).isEqualTo("test@example.com"); // No cambió
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ deleteById - Deletes existing user")
    void deleteById_ExistingUser_DeletesUser() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();
        Integer userId = testUser.getId();

        // WHEN: Eliminar usuario
        userRepository.deleteById(userId);
        entityManager.flush();

        // THEN: Usuario eliminado
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ delete - Deletes existing user entity")
    void delete_ExistingUser_DeletesUser() {
        // GIVEN: Persistir usuario
        entityManager.persist(testUser);
        entityManager.flush();

        // WHEN: Eliminar entidad
        User found = userRepository.findById(testUser.getId()).orElseThrow();
        userRepository.delete(found);
        entityManager.flush();

        // THEN: Usuario eliminado
        Optional<User> result = userRepository.findById(testUser.getId());
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS COUNT
    // ========================================

    @Test
    @DisplayName("✅ count - Returns correct count")
    void count_MultipleUsers_ReturnsCorrectCount() {
        // GIVEN: Persistir 3 usuarios
        for (int i = 1; i <= 3; i++) {
            User user = new User();
            user.setCorreo("user" + i + "@example.com");
            user.setNombre("User " + i);
            user.setHashContrasena("hash" + i);
            entityManager.persist(user);
        }
        entityManager.flush();

        // WHEN: Contar usuarios
        long count = userRepository.count();

        // THEN: Retornar 3
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ count - Empty database returns zero")
    void count_NoUsers_ReturnsZero() {
        // WHEN: No hay usuarios
        long count = userRepository.count();

        // THEN: Retornar 0
        assertThat(count).isZero();
    }
}
