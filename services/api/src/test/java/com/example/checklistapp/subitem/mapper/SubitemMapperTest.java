package com.example.checklistapp.subitem.mapper;

import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.task.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubitemMapper (MapStruct)
 *
 * CONCEPTOS CLAVE:
 * ================
 * 1. **Mappers.getMapper()**: Crea instancia del mapper generada por MapStruct
 * 2. **@Mapping(target = ..., ignore = true)**: Ignora campos que se asignan manualmente
 * 3. **Mapeo personalizado**: tarea.id → idTarea en ResponseDTO
 *
 * QUÉ PROBAMOS:
 * =============
 * - CreateSubitemDTO → Subitem (Entity) [ignora id, tarea, fechaCreacion]
 * - Subitem → SubitemResponseDTO [mapea tarea.id → idTarea]
 * - UpdateSubitemDTO → Subitem (actualización parcial)
 */
@DisplayName("SubitemMapper Integration Tests")
class SubitemMapperTest {

    private SubitemMapper subitemMapper;
    private Task testTask;
    private Subitem testSubitem;

    @BeforeEach
    void setUp() {
        // Usamos Mappers.getMapper() para crear instancia sin Spring
        subitemMapper = Mappers.getMapper(SubitemMapper.class);

        // Crear tarea de prueba
        testTask = new Task();
        testTask.setId(1);
        testTask.setNombre("Tarea principal");

        // Crear subitem de prueba
        testSubitem = new Subitem();
        testSubitem.setId(1);
        testSubitem.setTarea(testTask);
        testSubitem.setDescription("Subitem de prueba");
        testSubitem.setChecked(false);
        testSubitem.setFechaCreacion(LocalDateTime.now());
    }

    // ========================================
    // TESTS TO ENTITY (CREATE)
    // ========================================

    @Test
    @DisplayName("✅ toEntity - CreateSubitemDTO to Subitem (ignores id, tarea, fechaCreacion)")
    void toEntity_CreateSubitemDTO_ReturnsSubitemWithCorrectFields() {
        // GIVEN: CreateSubitemDTO
        CreateSubitemDTO dto = new CreateSubitemDTO(
                "Nuevo subitem",
                true
        );

        // WHEN: Mapear a Entity
        Subitem entity = subitemMapper.toEntity(dto);

        // THEN: Campos mapeados correctamente
        assertThat(entity.getDescription()).isEqualTo("Nuevo subitem");
        assertThat(entity.getChecked()).isTrue();

        // Campos ignorados por @Mapping(target = ..., ignore = true)
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTarea()).isNull();
        assertThat(entity.getFechaCreacion()).isNull();
    }

    @Test
    @DisplayName("✅ toEntity - Subitem with checked=false")
    void toEntity_SubitemWithCheckedFalse_MapsCorrectly() {
        // GIVEN: CreateSubitemDTO con checked=false
        CreateSubitemDTO dto = new CreateSubitemDTO(
                "Pendiente",
                false
        );

        // WHEN: Mapear a Entity
        Subitem entity = subitemMapper.toEntity(dto);

        // THEN: Checked es false
        assertThat(entity.getDescription()).isEqualTo("Pendiente");
        assertThat(entity.getChecked()).isFalse();
    }

    @Test
    @DisplayName("✅ toEntity - Empty description is preserved")
    void toEntity_EmptyDescription_PreservedInEntity() {
        // GIVEN: DTO con descripción vacía
        CreateSubitemDTO dto = new CreateSubitemDTO(
                "",
                false
        );

        // WHEN: Mapear
        Subitem entity = subitemMapper.toEntity(dto);

        // THEN: Descripción vacía preservada (validación ocurre en otro lado)
        assertThat(entity.getDescription()).isEqualTo("");
        assertThat(entity.getChecked()).isFalse();
    }

    // ========================================
    // TESTS TO RESPONSE DTO
    // ========================================

    @Test
    @DisplayName("✅ toResponseDTO - Subitem to SubitemResponseDTO (tarea.id → idTarea)")
    void toResponseDTO_Subitem_ReturnsDTOWithIdTarea() {
        // WHEN: Mapear a ResponseDTO
        SubitemResponseDTO dto = subitemMapper.toResponseDTO(testSubitem);

        // THEN: Campos básicos mapeados
        assertThat(dto.id()).isEqualTo(1);
        assertThat(dto.description()).isEqualTo("Subitem de prueba");
        assertThat(dto.checked()).isFalse();
        assertThat(dto.fechaCreacion()).isNotNull();

        // Mapeo personalizado: tarea.id → idTarea
        assertThat(dto.idTarea()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ toResponseDTO - Subitem with task")
    void toResponseDTO_SubitemWithTask_MapsTaskIdCorrectly() {
        // GIVEN: Subitem con tarea diferente
        Task task2 = new Task();
        task2.setId(5);
        testSubitem.setTarea(task2);

        // WHEN: Mapear a ResponseDTO
        SubitemResponseDTO dto = subitemMapper.toResponseDTO(testSubitem);

        // THEN: ID de tarea mapeado correctamente
        assertThat(dto.idTarea()).isEqualTo(5);
    }

    @Test
    @DisplayName("✅ toResponseDTO - Checked subitem")
    void toResponseDTO_CheckedSubitem_ReturnsDTOWithCheckedTrue() {
        // GIVEN: Subitem marcado como completado
        testSubitem.setChecked(true);
        testSubitem.setFechaCreacion(LocalDateTime.of(2024, 1, 15, 10, 30));

        // WHEN: Mapear a ResponseDTO
        SubitemResponseDTO dto = subitemMapper.toResponseDTO(testSubitem);

        // THEN: Checked es true
        assertThat(dto.checked()).isTrue();
        assertThat(dto.fechaCreacion()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
    }

    // ========================================
    // TESTS UPDATE ENTITY FROM DTO
    // ========================================

    @Test
    @DisplayName("✅ updateEntityFromDTO - Partial update (null fields ignored)")
    void updateEntityFromDTO_PartialUpdate_OnlyUpdatesNonNullFields() {
        // GIVEN: UpdateSubitemDTO parcial (solo cambiar description)
        UpdateSubitemDTO dto = new UpdateSubitemDTO(
                "Descripción actualizada",
                null  // checked null = no cambiar
        );

        Subitem entity = new Subitem();
        entity.setDescription("Descripción original");
        entity.setChecked(false);
        entity.setId(999); // Ignorado por @Mapping

        // WHEN: Actualizar entidad
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo campos no-null se actualizaron
        assertThat(updated.getDescription()).isEqualTo("Descripción actualizada"); // Cambió
        assertThat(updated.getChecked()).isFalse(); // No cambió (null en DTO)
        assertThat(updated.getId()).isEqualTo(999); // Ignorado
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Full update")
    void updateEntityFromDTO_FullUpdate_UpdatesAllFields() {
        // GIVEN: UpdateSubitemDTO completo
        UpdateSubitemDTO dto = new UpdateSubitemDTO(
                "Descripción nueva",
                true
        );

        Subitem entity = new Subitem();
        entity.setDescription("Original");
        entity.setChecked(false);

        // WHEN: Actualizar entidad
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Todos los campos actualizados
        assertThat(updated.getDescription()).isEqualTo("Descripción nueva");
        assertThat(updated.getChecked()).isTrue();
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Only update checked state")
    void updateEntityFromDTO_OnlyChecked_MapsOnlyChecked() {
        // GIVEN: DTO con solo checked
        UpdateSubitemDTO dto = new UpdateSubitemDTO(
                null,
                true
        );

        Subitem entity = new Subitem();
        entity.setDescription("Descripción original");
        entity.setChecked(false);

        // WHEN: Actualizar
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Solo checked cambió
        assertThat(updated.getDescription()).isEqualTo("Descripción original");
        assertThat(updated.getChecked()).isTrue();
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Toggle checked from false to true")
    void updateEntityFromDTO_ToggleCheckedToTrue_ChangesState() {
        // GIVEN: DTO para marcar como completado
        UpdateSubitemDTO dto = new UpdateSubitemDTO(
                null,
                true
        );

        Subitem entity = new Subitem();
        entity.setDescription("Tarea pendiente");
        entity.setChecked(false);

        // WHEN: Actualizar
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Estado cambiado
        assertThat(updated.getChecked()).isTrue();
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Toggle checked from true to false")
    void updateEntityFromDTO_ToggleCheckedToFalse_ChangesState() {
        // GIVEN: DTO para desmarcar
        UpdateSubitemDTO dto = new UpdateSubitemDTO(
                null,
                false
        );

        Subitem entity = new Subitem();
        entity.setDescription("Tarea completada");
        entity.setChecked(true);

        // WHEN: Actualizar
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Estado cambiado
        assertThat(updated.getChecked()).isFalse();
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - All nulls DTO does not modify entity")
    void updateEntityFromDTO_AllNulls_DoesNotModifyEntity() {
        // GIVEN: DTO con todos los campos null
        UpdateSubitemDTO dto = new UpdateSubitemDTO(null, null);

        Subitem entity = new Subitem();
        entity.setDescription("Original");
        entity.setChecked(false);

        // WHEN: Actualizar
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Ningún campo modificado (strategy IGNORE)
        assertThat(updated.getDescription()).isEqualTo("Original");
        assertThat(updated.getChecked()).isFalse();
    }

    // ========================================
    // TESTS EDGE CASES
    // ========================================

    @Test
    @DisplayName("✅ toEntity - Very long description")
    void toEntity_VeryLongDescription_MapsCorrectly() {
        // GIVEN: DTO con descripción larga
        String longDesc = "a".repeat(500);
        CreateSubitemDTO dto = new CreateSubitemDTO(
                longDesc,
                false
        );

        // WHEN: Mapear
        Subitem entity = subitemMapper.toEntity(dto);

        // THEN: Descripción larga preservada
        assertThat(entity.getDescription()).hasSize(500);
    }

    @Test
    @DisplayName("✅ toResponseDTO - Subitem with null task (edge case)")
    void toResponseDTO_SubitemWithNullTask_ReturnsDTOWithNullIdTarea() {
        // GIVEN: Subitem sin tarea (caso edge, no debería ocurrir normalmente)
        testSubitem.setTarea(null);

        // WHEN: Mapear a ResponseDTO
        SubitemResponseDTO dto = subitemMapper.toResponseDTO(testSubitem);

        // THEN: idTarea es null (porque tarea.id no existe)
        assertThat(dto.idTarea()).isNull();
    }

    @Test
    @DisplayName("✅ updateEntityFromDTO - Ignores id, tarea, fechaCreacion")
    void updateEntityFromDTO_DoesNotUpdateIgnoredFields() {
        // GIVEN: UpdateSubitemDTO
        UpdateSubitemDTO dto = new UpdateSubitemDTO("Nueva desc", true);

        Subitem entity = new Subitem();
        entity.setId(100);
        entity.setTarea(testTask);
        entity.setFechaCreacion(LocalDateTime.of(2024, 1, 1, 0, 0));
        entity.setDescription("Original");
        entity.setChecked(false);

        // WHEN: Actualizar
        Subitem updated = subitemMapper.updateEntityFromDTO(dto, entity);

        // THEN: Campos ignorados no cambian (@Mapping(target = ..., ignore = true))
        assertThat(updated.getId()).isEqualTo(100);
        assertThat(updated.getTarea()).isEqualTo(testTask);
        assertThat(updated.getFechaCreacion()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));

        // Campos normales sí cambian
        assertThat(updated.getDescription()).isEqualTo("Nueva desc");
        assertThat(updated.getChecked()).isTrue();
    }
}
