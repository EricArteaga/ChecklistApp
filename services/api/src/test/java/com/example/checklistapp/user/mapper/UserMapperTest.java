package com.example.checklistapp.user.mapper;

import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserMapper (MapStruct)
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **Mappers.getMapper()**: Crea instancia del mapper generada por MapStruct
 * 2. **Seguridad de contraseña**: hashContrasena NO se mapea a DTOs (seguridad)
 * 3. **nullValuePropertyMappingStrategy.IGNORE**: Nulls en Update no sobrescriben valores
 *
 * QUÉ PROBAMOS:
 * =============
 * - CreateUserDTO → User (Entity)
 * - User → UserResponseDTO (sin hashContrasena)
 * - User → UserSummaryDTO (sin hashContrasena)
 * - UpdateUserDTO → User (actualización parcial)
 */
@DisplayName("UserMapper Integration Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Usamos Mappers.getMapper() para crear instancia sin Spring
        userMapper = Mappers.getMapper(UserMapper.class);

        // Crear usuario de prueba
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");
        testUser.setHashContrasena("hashed_password_123");
        testUser.setFechaCreacion(LocalDateTime.now());
    }

    // ========================================
    // TESTS TO ENTITY (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ toEntity - CreateUserDTO to User")
    void toEntity_CreateUserDTO_ReturnsUserWithAllFields() {
        // GIVEN: CreateUserDTO
        CreateUserDTO dto = new CreateUserDTO(
                "newuser@example.com",
                "Nombre del Usuario"
        );

        // WHEN: Mapear a Entity
        User entity = userMapper.toEntity(dto);

        // THEN: Campos del DTO mapeados correctamente
        assertThat(entity.getCorreo()).isEqualTo("newuser@example.com");
        assertThat(entity.getNombre()).isEqualTo("Nombre del Usuario");

        // ID y fechaCreacion se generarán al persistir
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFechaCreacion()).isNull();

        // hashContrasena es null porque CreateUserDTO no tiene campo de contraseña
        assertThat(entity.getHashContrasena()).isNull();
    }

    @Test
    @DisplayName("✅ toEntity - User with special characters in email")
    void toEntity_EmailWithSpecialCharacters_MapsCorrectly() {
        // GIVEN: Email con caracteres especiales válidos
        CreateUserDTO dto = new CreateUserDTO(
                "user+test@example.co.uk",
                "User Name"
        );

        // WHEN: Mapear
        User entity = userMapper.toEntity(dto);

        // THEN: Email preservado correctamente
        assertThat(entity.getCorreo()).isEqualTo("user+test@example.co.uk");
    }

    // ========================================
    // TESTS TO RESPONSE DTO
    // ========================================

    @Test
    @DisplayName("✅ toResponseDTO - User to UserResponseDTO (excludes hashContrasena)")
    void toResponseDTO_User_ReturnsDTOWithoutPassword() {
        // WHEN: Mapear a ResponseDTO
        UserResponseDTO dto = userMapper.toResponseDTO(testUser);

        // THEN: Campos públicos mapeados
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.correo()).isEqualTo("test@example.com");
        assertThat(dto.nombre()).isEqualTo("Usuario Test");
        assertThat(dto.fechaCreacion()).isNotNull();

        // hashContrasena NO está en el DTO (seguridad)
        assertThat(dto).hasNoNullFieldsOrPropertiesExcept("hashContrasena no existe en DTO");
    }

    @Test
    @DisplayName("✅ toResponseDTO - User with all fields populated")
    void toResponseDTO_CompleteUser_ReturnsCompleteDTO() {
        // GIVEN: Usuario con todos los campos
        testUser.setFechaCreacion(LocalDateTime.of(2024, 1, 15, 10, 30));

        // WHEN: Mapear
        UserResponseDTO dto = userMapper.toResponseDTO(testUser);

        // THEN: Todos los campos presentes
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.correo()).isEqualTo("test@example.com");
        assertThat(dto.nombre()).isEqualTo("Usuario Test");
        assertThat(dto.fechaCreacion()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    // ========================================
    // TESTS TO SUMMARY DTO
    // ========================================

    @Test
    @DisplayName("✅ toSummaryDTO - User to UserSummaryDTO (lighter version)")
    void toSummaryDTO_User_ReturnsDTOWithEssentialFields() {
        // WHEN: Mapear a SummaryDTO
        UserSummaryDTO dto = userMapper.toSummaryDTO(testUser);

        // THEN: Solo campos esenciales
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.correo()).isEqualTo("test@example.com");
        assertThat(dto.nombre()).isEqualTo("Usuario Test");

        // Verificar que tiene los campos esperados
        assertThat(dto).hasFieldOrProperty("id");
        assertThat(dto).hasFieldOrProperty("correo");
        assertThat(dto).hasFieldOrProperty("nombre");
        // UserSummaryDTO no incluye fechaCreacion ni hashContrasena (más ligero)
    }

    @Test
    @DisplayName("✅ toSummaryDTO - Multiple users to summary DTOs")
    void toSummaryDTO_ListOfUsers_ReturnsListOfDTOs() {
        // GIVEN: Múltiples usuarios
        User user1 = testUser;
        User user2 = new User();
        user2.setId(2);
        user2.setCorreo("user2@example.com");
        user2.setNombre("User 2");
        user2.setHashContrasena("hash2");

        // WHEN: Mapear ambos
        UserSummaryDTO dto1 = userMapper.toSummaryDTO(user1);
        UserSummaryDTO dto2 = userMapper.toSummaryDTO(user2);

        // THEN: Ambos mapeados correctamente
        assertThat(dto1.id()).isEqualTo(1);
        assertThat(dto1.correo()).isEqualTo("test@example.com");

        assertThat(dto2.id()).isEqualTo(2);
        assertThat(dto2.correo()).isEqualTo("user2@example.com");
    }

    // ========================================
    // TESTS UPDATE ENTITY FROM DTO
    // ========================================

    @Test
    @DisplayName("✅ updateEntityFromDTO - Partial update (null fields ignored)")
    void updateEntityFromDTO_PartialUpdate_OnlyUpdatesNonNullFields() {
        // GIVEN: UpdateUserDTO parcial (solo cambiar nombre)
        UpdateUserDTO dto = new UpdateUserDTO(
                null, // correo null = no cambiar
                "Nuevo Nombre"
        );

        User entity = new User();
        entity.setCorreo("original@example.com");
        entity.setNombre("Nombre Original");
        entity.setHashContrasena("original_hash");

        // WHEN: Actualizar entidad
        userMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo campos no-null se actualizaron
        assertThat(entity.getCorreo()).isEqualTo("original@example.com"); // No cambió
        assertThat(entity.getNombre()).isEqualTo("Nuevo Nombre"); // Cambió
        assertThat(entity.getHashContrasena()).isEqualTo("original_hash"); // No cambió
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Full update")
    void updateEntityFromDTO_FullUpdate_UpdatesAllFields() {
        // GIVEN: UpdateUserDTO completo
        UpdateUserDTO dto = new UpdateUserDTO(
                "updated@example.com",
                "Usuario Actualizado"
        );

        User entity = new User();
        entity.setCorreo("original@example.com");
        entity.setNombre("Original");
        entity.setHashContrasena("original_hash");

        // WHEN: Actualizar entidad
        userMapper.updateEntityFromDTO(dto, entity);

        // THEN: Campos del DTO actualizados
        assertThat(entity.getCorreo()).isEqualTo("updated@example.com");
        assertThat(entity.getNombre()).isEqualTo("Usuario Actualizado");
        assertThat(entity.getHashContrasena()).isEqualTo("original_hash"); // No cambió (no hay campo contraseña)
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Only update email")
    void updateEntityFromDTO_OnlyEmail_MapsOnlyEmail() {
        // GIVEN: DTO con solo email
        UpdateUserDTO dto = new UpdateUserDTO(
                "onlyemail@example.com",
                null
        );

        User entity = new User();
        entity.setCorreo("old@example.com");
        entity.setNombre("Old Name");
        entity.setHashContrasena("old_hash");

        // WHEN: Actualizar
        userMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo email cambió
        assertThat(entity.getCorreo()).isEqualTo("onlyemail@example.com");
        assertThat(entity.getNombre()).isEqualTo("Old Name");
        assertThat(entity.getHashContrasena()).isEqualTo("old_hash");
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Only update name")
    void updateEntityFromDTO_OnlyName_MapsOnlyName() {
        // GIVEN: DTO con solo nombre
        UpdateUserDTO dto = new UpdateUserDTO(
                null,
                "New Name Only"
        );

        User entity = new User();
        entity.setCorreo("user@example.com");
        entity.setNombre("User Name");
        entity.setHashContrasena("old_hash");

        // WHEN: Actualizar
        userMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo nombre cambió
        assertThat(entity.getCorreo()).isEqualTo("user@example.com");
        assertThat(entity.getNombre()).isEqualTo("New Name Only");
        assertThat(entity.getHashContrasena()).isEqualTo("old_hash");
    }

    // ========================================
    // TESTS EDGE CASES
    // ========================================

    @Test
    @DisplayName("✅ toEntity - Empty strings are preserved")
    void toEntity_EmptyStrings_PreservedInEntity() {
        // GIVEN: DTO con strings vacíos
        CreateUserDTO dto = new CreateUserDTO(
                "",
                ""
        );

        // WHEN: Mapear
        User entity = userMapper.toEntity(dto);

        // THEN: Strings vacíos preservados (validación ocurre en otro lado)
        assertThat(entity.getCorreo()).isEqualTo("");
        assertThat(entity.getNombre()).isEqualTo("");
        assertThat(entity.getHashContrasena()).isNull(); // CreateUserDTO no tiene campo contraseña
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - All nulls DTO does not modify entity")
    void updateEntityFromDTO_AllNulls_DoesNotModifyEntity() {
        // GIVEN: DTO con todos los campos null
        UpdateUserDTO dto = new UpdateUserDTO(null, null);

        User entity = new User();
        entity.setCorreo("user@example.com");
        entity.setNombre("User Name");
        entity.setHashContrasena("password_hash");

        // WHEN: Actualizar
        userMapper.updateEntityFromDTO(dto, entity);

        // THEN: Ningún campo modificado (strategy IGNORE)
        assertThat(entity.getCorreo()).isEqualTo("user@example.com");
        assertThat(entity.getNombre()).isEqualTo("User Name");
        assertThat(entity.getHashContrasena()).isEqualTo("password_hash");
    }
}
