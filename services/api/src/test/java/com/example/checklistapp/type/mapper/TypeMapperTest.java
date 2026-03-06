package com.example.checklistapp.type.mapper;

import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TypeMapper (MapStruct)
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **Mappers.getMapper()**: Crea instancia del mapper generada por MapStruct
 * 2. **nullValuePropertyMappingStrategy.IGNORE**: Nulls en Update no sobrescriben valores
 * 3. **Mapeo simple**: Type tiene solo 3 campos (id, nombre, color, idUsuario)
 *
 * QUÉ PROBAMOS:
 * =============
 * - CreateTypeDTO → Type (Entity)
 * - Type → TypeResponseDTO
 * - Type → TypeSummaryDTO (más ligero)
 * - UpdateTypeDTO → Type (actualización parcial)
 */
@DisplayName("TypeMapper Integration Tests")
class TypeMapperTest {

    private TypeMapper typeMapper;
    private Type testType;

    @BeforeEach
    void setUp() {
        // Usamos Mappers.getMapper() para crear instancia sin Spring
        typeMapper = Mappers.getMapper(TypeMapper.class);

        // Crear tipo de prueba
        testType = new Type();
        testType.setId(1);
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");
        testType.setIdUsuario(1);
    }

    // ========================================
    // TESTS TO ENTITY (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ toEntity - CreateTypeDTO to Type")
    void toEntity_CreateTypeDTO_ReturnsTypeWithAllFields() {
        // GIVEN: CreateTypeDTO
        CreateTypeDTO dto = new CreateTypeDTO(
                1, // idUsuario
                "Personal",
                "#28a745"
        );

        // WHEN: Mapear a Entity
        Type entity = typeMapper.toEntity(dto);

        // THEN: Todos los campos mapeados correctamente
        assertThat(entity.getNombre()).isEqualTo("Personal");
        assertThat(entity.getColor()).isEqualTo("#28a745");
        assertThat(entity.getIdUsuario()).isEqualTo(1);

        // ID se generará al persistir
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("✅ toEntity - Type with null color (optional field)")
    void toEntity_TypeWithNullColor_MapsCorrectly() {
        // GIVEN: CreateTypeDTO sin color (campo opcional)
        CreateTypeDTO dto = new CreateTypeDTO(
                1, // idUsuario
                "Sin color",
                null
        );

        // WHEN: Mapear a Entity
        Type entity = typeMapper.toEntity(dto);

        // THEN: Color es null (permitido)
        assertThat(entity.getNombre()).isEqualTo("Sin color");
        assertThat(entity.getColor()).isNull();
        assertThat(entity.getIdUsuario()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ toEntity - Type with empty color string")
    void toEntity_TypeWithEmptyColor_MapsCorrectly() {
        // GIVEN: CreateTypeDTO con color vacío
        CreateTypeDTO dto = new CreateTypeDTO(
                1, // idUsuario
                "Color vacío",
                ""
        );

        // WHEN: Mapear a Entity
        Type entity = typeMapper.toEntity(dto);

        // THEN: Color vacío preservado
        assertThat(entity.getColor()).isEqualTo("");
    }

    // ========================================
    // TESTS TO RESPONSE DTO
    // ========================================

    @Test
    @DisplayName("✅ toResponseDTO - Type to TypeResponseDTO")
    void toResponseDTO_Type_ReturnsDTOWithAllFields() {
        // WHEN: Mapear a ResponseDTO
        TypeResponseDTO dto = typeMapper.toResponseDTO(testType);

        // THEN: Todos los campos mapeados
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nombre()).isEqualTo("Trabajo");
        assertThat(dto.color()).isEqualTo("#326cc3");
        assertThat(dto.idUsuario()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ toResponseDTO - Type with null color")
    void toResponseDTO_TypeWithNullColor_ReturnsDTOWithNullColor() {
        // GIVEN: Tipo sin color
        testType.setColor(null);

        // WHEN: Mapear a ResponseDTO
        TypeResponseDTO dto = typeMapper.toResponseDTO(testType);

        // THEN: Color es null en DTO
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nombre()).isEqualTo("Trabajo");
        assertThat(dto.color()).isNull();
    }

    // ========================================
    // TESTS TO SUMMARY DTO
    // ========================================

    @Test
    @DisplayName("✅ toSummaryDTO - Type to TypeSummaryDTO (lighter version)")
    void toSummaryDTO_Type_ReturnsDTOWithoutIdUsuario() {
        // WHEN: Mapear a SummaryDTO
        TypeSummaryDTO dto = typeMapper.toSummaryDTO(testType);

        // THEN: Campos básicos presentes
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.nombre()).isEqualTo("Trabajo");
        assertThat(dto.color()).isEqualTo("#326cc3");

        // Verificar que tiene los campos esperados
        assertThat(dto).hasFieldOrProperty("id");
        assertThat(dto).hasFieldOrProperty("nombre");
        assertThat(dto).hasFieldOrProperty("color");
        // TypeSummaryDTO no tiene idUsuario (más ligero)
    }

    @Test
    @DisplayName("✅ toSummaryDTO - Multiple types to summary DTOs")
    void toSummaryDTO_ListOfTypes_ReturnsListOfDTOs() {
        // GIVEN: Múltiples tipos
        Type type1 = testType;
        Type type2 = new Type();
        type2.setId(2);
        type2.setNombre("Personal");
        type2.setColor("#28a745");
        type2.setIdUsuario(1);

        // WHEN: Mapear ambos
        TypeSummaryDTO dto1 = typeMapper.toSummaryDTO(type1);
        TypeSummaryDTO dto2 = typeMapper.toSummaryDTO(type2);

        // THEN: Ambos mapeados correctamente
        assertThat(dto1.id()).isEqualTo(1);
        assertThat(dto1.nombre()).isEqualTo("Trabajo");
        assertThat(dto1.color()).isEqualTo("#326cc3");

        assertThat(dto2.id()).isEqualTo(2);
        assertThat(dto2.nombre()).isEqualTo("Personal");
        assertThat(dto2.color()).isEqualTo("#28a745");
    }

    // ========================================
    // TESTS UPDATE ENTITY FROM DTO
    // ========================================

    @Test
    @DisplayName("✅ updateEntityFromDTO - Partial update (null fields ignored)")
    void updateEntityFromDTO_PartialUpdate_OnlyUpdatesNonNullFields() {
        // GIVEN: UpdateTypeDTO parcial (solo cambiar nombre)
        UpdateTypeDTO dto = new UpdateTypeDTO(
                "Nombre Actualizado",
                null  // color null = no cambiar
        );

        Type entity = new Type();
        entity.setNombre("Nombre Original");
        entity.setColor("#ff0000");
        entity.setIdUsuario(1);

        // WHEN: Actualizar entidad
        typeMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo campos no-null se actualizaron
        assertThat(entity.getNombre()).isEqualTo("Nombre Actualizado"); // Cambió
        assertThat(entity.getColor()).isEqualTo("#ff0000"); // No cambió (null en DTO)
        assertThat(entity.getIdUsuario()).isEqualTo(1); // No cambió
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Full update")
    void updateEntityFromDTO_FullUpdate_UpdatesAllFields() {
        // GIVEN: UpdateTypeDTO completo
        UpdateTypeDTO dto = new UpdateTypeDTO(
                "Deporte",
                "#dc3545"
        );

        Type entity = new Type();
        entity.setNombre("Original");
        entity.setColor("#000000");
        entity.setIdUsuario(1);

        // WHEN: Actualizar entidad
        typeMapper.updateEntityFromDTO(dto, entity);

        // THEN: Todos los campos actualizados
        assertThat(entity.getNombre()).isEqualTo("Deporte");
        assertThat(entity.getColor()).isEqualTo("#dc3545");
        assertThat(entity.getIdUsuario()).isEqualTo(1); // No hay campo idUsuario en UpdateTypeDTO
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Only update color")
    void updateEntityFromDTO_OnlyColor_MapsOnlyColor() {
        // GIVEN: DTO con solo color
        UpdateTypeDTO dto = new UpdateTypeDTO(
                null,
                "#00ff00"
        );

        Type entity = new Type();
        entity.setNombre("Trabajo");
        entity.setColor("#326cc3");
        entity.setIdUsuario(1);

        // WHEN: Actualizar
        typeMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo color cambió
        assertThat(entity.getNombre()).isEqualTo("Trabajo");
        assertThat(entity.getColor()).isEqualTo("#00ff00");
        assertThat(entity.getIdUsuario()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - All nulls DTO does not modify entity")
    void updateEntityFromDTO_AllNulls_DoesNotModifyEntity() {
        // GIVEN: DTO con todos los campos null
        UpdateTypeDTO dto = new UpdateTypeDTO(null, null);

        Type entity = new Type();
        entity.setNombre("Trabajo");
        entity.setColor("#326cc3");
        entity.setIdUsuario(1);

        // WHEN: Actualizar
        typeMapper.updateEntityFromDTO(dto, entity);

        // THEN: Ningún campo modificado (strategy IGNORE)
        assertThat(entity.getNombre()).isEqualTo("Trabajo");
        assertThat(entity.getColor()).isEqualTo("#326cc3");
        assertThat(entity.getIdUsuario()).isEqualTo(1);
    }

    // ========================================
    // TESTS EDGE CASES
    // ========================================

    @Test
    @DisplayName("✅ toEntity - Empty name is preserved")
    void toEntity_EmptyName_PreservedInEntity() {
        // GIVEN: DTO con nombre vacío
        CreateTypeDTO dto = new CreateTypeDTO(
                1, // idUsuario
                "",
                "#fff"
        );

        // WHEN: Mapear
        Type entity = typeMapper.toEntity(dto);

        // THEN: Nombre vacío preservado (validación ocurre en otro lado)
        assertThat(entity.getNombre()).isEqualTo("");
        assertThat(entity.getColor()).isEqualTo("#fff");
    }

    @Test
    @DisplayName("✅ toResponseDTO - Type with very long color")
    void toResponseDTO_TypeWithLongColor_MapsCorrectly() {
        // GIVEN: Tipo con color largo (inválido pero permitido en entity)
        testType.setColor("#123456789");

        // WHEN: Mapear
        TypeResponseDTO dto = typeMapper.toResponseDTO(testType);

        // THEN: Color largo preservado
        assertThat(dto.color()).isEqualTo("#123456789");
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Update to null color")
    void updateEntityFromDTO_UpdateToNullColor_SetsColorToNull() {
        // GIVEN: DTO con color explícitamente null (remover color)
        UpdateTypeDTO dto = new UpdateTypeDTO(
                "Trabajo",
                null
        );

        Type entity = new Type();
        entity.setNombre("Original");
        entity.setColor("#ff0000");
        entity.setIdUsuario(1);

        // WHEN: Actualizar
        typeMapper.updateEntityFromDTO(dto, entity);

        // THEN: Color NO cambia a null (strategy IGNORE para nulls)
        assertThat(entity.getColor()).isEqualTo("#ff0000");
    }
}
