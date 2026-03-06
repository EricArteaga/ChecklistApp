package com.example.checklistapp.type.repository;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TypeRepository
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **@DataJpaTest**: Configura BD H2 y repositorios para testing
 * 2. **TestEntityManager**: Permite persistir entidades en la BD de test
 * 3. **Métodos derivados**: findByIdUsuario es generado por Spring Data JPA
 *
 * QUÉ PROBAMOS:
 * =============
 * - Consultas por usuario (findByIdUsuario)
 * - Operaciones CRUD básicas
 * - Relación con usuarios (idUsuario)
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TypeRepository Integration Tests")
class TypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TypeRepository typeRepository;

    private User testUser;
    private Type testType;

    @BeforeEach
    void setUp() {
        // Crear y persistir usuario primero (Type depende de User)
        testUser = new User();
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");
        testUser.setHashContrasena("hash123");
        entityManager.persist(testUser);
        entityManager.flush();

        // Crear tipo de prueba
        testType = new Type();
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");
        testType.setIdUsuario(testUser.getId());
    }

    // ========================================
    // TESTS FIND BY ID USUARIO
    // ========================================

    @Test
    @DisplayName("✅ findByIdUsuario - Returns all types for user")
    void findByIdUsuario_ExistingUser_ReturnsAllUserTypes() {
        // GIVEN: Usuario con múltiples tipos
        Type type1 = createType("Trabajo", "#326cc3");
        Type type2 = createType("Personal", "#28a745");
        Type type3 = createType("Estudio", "#ffc107");

        entityManager.persist(type1);
        entityManager.persist(type2);
        entityManager.persist(type3);
        entityManager.flush();

        // WHEN: Buscar tipos por usuario
        List<Type> types = typeRepository.findByIdUsuario(testUser.getId());

        // THEN: Retornar todos los tipos del usuario
        assertThat(types).hasSize(3);
        assertThat(types).extracting(Type::getNombre)
                .containsExactlyInAnyOrder("Trabajo", "Personal", "Estudio");
    }

    @Test
    @DisplayName("❌ findByIdUsuario - Non-existent user returns empty list")
    void findByIdUsuario_NonExistentUser_ReturnsEmptyList() {
        // GIVEN: No persistir tipos

        // WHEN: Buscar tipos de usuario inexistente
        List<Type> types = typeRepository.findByIdUsuario(999);

        // THEN: Retornar lista vacía
        assertThat(types).isEmpty();
    }

    @Test
    @DisplayName("✅ findByIdUsuario - Returns only types for specific user")
    void findByIdUsuario_MultipleUsers_ReturnsOnlyUserTypes() {
        // GIVEN: Crear otro usuario con sus tipos
        User user2 = new User();
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");
        user2.setHashContrasena("hash2");
        entityManager.persist(user2);
        entityManager.flush();

        // Tipos del primer usuario
        Type type1 = createType("Trabajo", "#326cc3");
        entityManager.persist(type1);

        // Tipos del segundo usuario
        Type type2 = new Type();
        type2.setNombre("Deporte");
        type2.setColor("#dc3545");
        type2.setIdUsuario(user2.getId());
        entityManager.persist(type2);

        entityManager.flush();

        // WHEN: Buscar tipos del primer usuario
        List<Type> user1Types = typeRepository.findByIdUsuario(testUser.getId());

        // THEN: Retornar solo tipos del primer usuario
        assertThat(user1Types).hasSize(1);
        assertThat(user1Types.get(0).getNombre()).isEqualTo("Trabajo");
        assertThat(user1Types.get(0).getIdUsuario()).isEqualTo(testUser.getId());
    }

    // ========================================
    // TESTS SAVE (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ save - Creates type with generated ID")
    void save_ValidType_CreatesTypeWithId() {
        // GIVEN: Tipo sin ID
        assertThat(testType.getId()).isNull();

        // WHEN: Guardar tipo
        Type saved = typeRepository.save(testType);

        // THEN: Asignar ID
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("Trabajo");
        assertThat(saved.getColor()).isEqualTo("#326cc3");
        assertThat(saved.getIdUsuario()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("✅ save - Multiple types auto-increment IDs")
    void save_MultipleTypes_AutoIncrementIds() {
        // GIVEN: Crear múltiples tipos
        Type type1 = createType("Trabajo", "#326cc3");
        Type type2 = createType("Personal", "#28a745");

        // WHEN: Persistir ambos
        typeRepository.save(type1);
        typeRepository.save(type2);
        entityManager.flush();

        // THEN: Tener IDs consecutivos
        assertThat(type1.getId()).isNotNull();
        assertThat(type2.getId()).isNotNull();
        assertThat(type2.getId()).isEqualTo(type1.getId() + 1);
    }

    @Test
    @DisplayName("✅ save - Type with null color is valid")
    void save_TypeWithNullColor_SavesSuccessfully() {
        // GIVEN: Tipo sin color (color es opcional)
        Type typeWithoutColor = createType("Sin color", null);

        // WHEN: Guardar
        Type saved = typeRepository.save(typeWithoutColor);
        entityManager.flush();

        // THEN: Persistir correctamente
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getColor()).isNull();
    }

    // ========================================
    // TESTS FIND BY ID
    // ========================================

    @Test
    @DisplayName("✅ findById - Existing type returns type")
    void findById_ExistingType_ReturnsType() {
        // GIVEN: Persistir tipo
        entityManager.persist(testType);
        entityManager.flush();

        // WHEN: Buscar por ID
        Optional<Type> result = typeRepository.findById(testType.getId());

        // THEN: Retornar tipo
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Trabajo");
        assertThat(result.get().getColor()).isEqualTo("#326cc3");
        assertThat(result.get().getIdUsuario()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("❌ findById - Non-existent ID returns empty")
    void findById_NonExistentId_ReturnsEmpty() {
        // WHEN: Buscar ID inexistente
        Optional<Type> result = typeRepository.findById(999);

        // THEN: Retornar empty
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS FIND ALL
    // ========================================

    @Test
    @DisplayName("✅ findAll - Returns all types from all users")
    void findAll_MultipleTypesFromMultipleUsers_ReturnsAllTypes() {
        // GIVEN: Crear tipos para múltiples usuarios
        User user2 = new User();
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");
        user2.setHashContrasena("hash2");
        entityManager.persist(user2);
        entityManager.flush();

        Type type1 = createType("Trabajo", "#326cc3");
        entityManager.persist(type1);

        Type type2 = new Type();
        type2.setNombre("Deporte");
        type2.setColor("#dc3545");
        type2.setIdUsuario(user2.getId());
        entityManager.persist(type2);

        entityManager.flush();

        // WHEN: Buscar todos
        List<Type> types = typeRepository.findAll();

        // THEN: Retornar todos los tipos de todos los usuarios
        assertThat(types).hasSize(2);
        assertThat(types).extracting(Type::getNombre)
                .containsExactlyInAnyOrder("Trabajo", "Deporte");
    }

    @Test
    @DisplayName("✅ findAll - Empty database returns empty list")
    void findAll_NoTypes_ReturnsEmptyList() {
        // WHEN: No hay tipos en BD
        List<Type> types = typeRepository.findAll();

        // THEN: Retornar lista vacía
        assertThat(types).isEmpty();
    }

    // ========================================
    // TESTS UPDATE
    // ========================================

    @Test
    @DisplayName("✅ save - Updates existing type")
    void save_ExistingType_UpdatesType() {
        // GIVEN: Persistir tipo
        entityManager.persist(testType);
        entityManager.flush();
        entityManager.clear();

        // WHEN: Actualizar nombre y color
        Type found = typeRepository.findById(testType.getId()).orElseThrow();
        found.setNombre("Nombre Actualizado");
        found.setColor("#ff0000");
        typeRepository.save(found);
        entityManager.flush();
        entityManager.clear();

        // THEN: Cambios persistidos
        Type updated = typeRepository.findById(testType.getId()).orElseThrow();
        assertThat(updated.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(updated.getColor()).isEqualTo("#ff0000");
        assertThat(updated.getIdUsuario()).isEqualTo(testUser.getId()); // No cambió
    }

    // ========================================
    // TESTS DELETE
    // ========================================

    @Test
    @DisplayName("✅ deleteById - Deletes existing type")
    void deleteById_ExistingType_DeletesType() {
        // GIVEN: Persistir tipo
        entityManager.persist(testType);
        entityManager.flush();
        Integer typeId = testType.getId();

        // WHEN: Eliminar tipo
        typeRepository.deleteById(typeId);
        entityManager.flush();

        // THEN: Tipo eliminado
        Optional<Type> result = typeRepository.findById(typeId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ delete - Deletes existing type entity")
    void delete_ExistingType_DeletesType() {
        // GIVEN: Persistir tipo
        entityManager.persist(testType);
        entityManager.flush();

        // WHEN: Eliminar entidad
        Type found = typeRepository.findById(testType.getId()).orElseThrow();
        typeRepository.delete(found);
        entityManager.flush();

        // THEN: Tipo eliminado
        Optional<Type> result = typeRepository.findById(testType.getId());
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS COUNT
    // ========================================

    @Test
    @DisplayName("✅ count - Returns correct count")
    void count_MultipleTypes_ReturnsCorrectCount() {
        // GIVEN: Persistir 3 tipos
        for (int i = 1; i <= 3; i++) {
            Type type = createType("Tipo " + i, "#ff00" + i);
            entityManager.persist(type);
        }
        entityManager.flush();

        // WHEN: Contar tipos
        long count = typeRepository.count();

        // THEN: Retornar 3
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ count - Empty database returns zero")
    void count_NoTypes_ReturnsZero() {
        // WHEN: No hay tipos
        long count = typeRepository.count();

        // THEN: Retornar 0
        assertThat(count).isZero();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private Type createType(String nombre, String color) {
        Type type = new Type();
        type.setNombre(nombre);
        type.setColor(color);
        type.setIdUsuario(testUser.getId());
        return type;
    }
}
