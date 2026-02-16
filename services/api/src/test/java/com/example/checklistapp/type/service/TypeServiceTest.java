package com.example.checklistapp.type.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.mapper.TypeMapper;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.type.repository.TypeRepository;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TypeService
 *
 * SIMILITUDES CON UserService:
 * ============================
 * - CRUD básico (create, findById, findAll, update, delete)
 * - Protección contra eliminación cuando hay tareas asociadas
 * - Validación de que hay cambios en UpdateDTO
 *
 * DIFERENCIAS CON UserService:
 * ============================
 * - Type pertenece a un User (idUsuario es requerido)
 * - NO hay validación de unicidad (puede haber tipos con mismo nombre)
 * - findByIdUsuario: lista tipos de un usuario específico
 *
 * VALIDACIONES ESPECÍFICAS DE TypeService:
 * =========================================
 * 1. Usuario debe existir al crear tipo (referencia válida)
 * 2. No se puede eliminar tipo si tiene tareas asociadas
 * 3. UpdateDTO debe tener al menos un campo modificado
 */
@ExtendWith(MockitoExtension.class)
class TypeServiceTest {

    // === MOCKS ===
    @Mock
    private TypeRepository typeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TypeMapper typeMapper;

    // === SYSTEM UNDER TEST (SUT) ===
    @InjectMocks
    private TypeService typeService;

    // === FIXTURE ===
    private User testUser;
    private Type testType;
    private CreateTypeDTO createTypeDTO;
    private TypeResponseDTO typeResponseDTO;

    /**
     * @BeforeEach: Configura objetos de prueba ANTES de CADA test
     *
     * FLUJO DE DATOS:
     * User (1) → Type (1, "Trabajo", "#326cc3") → TypeResponseDTO
     *
     * RELACIÓN:
     * Type pertenece a User (muchos tipos por usuario)
     */
    @BeforeEach
    void setUp() {
        // Usuario dueño del tipo
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");

        // Tipo de prueba (categoría de tarea)
        testType = new Type();
        testType.setId(1);
        testType.setIdUsuario(1); // Tipo pertenece a usuario ID 1
        testType.setNombre("Trabajo");
        testType.setColor("#326cc3");

        // DTO para crear tipo (payload HTTP POST)
        // CreateTypeDTO: Integer idUsuario, String nombre, String color
        createTypeDTO = new CreateTypeDTO(
                1,      // idUsuario (dueño del tipo)
                "Trabajo",
                "#326cc3"
        );

        // DTO de respuesta esperado (TypeResponseDTO: id, idUsuario, nombre, color)
        typeResponseDTO = new TypeResponseDTO(
                1,
                1, // idUsuario
                "Trabajo",
                "#326cc3"
        );
    }

    // ========================================
    // TESTS CREATE: Creación de tipos
    // ========================================

    @Test
    @DisplayName("✅ create() - Crea tipo exitosamente cuando usuario existe")
    void create_WhenUserExists_CreatesType() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Happy path de creación: usuario válido → tipo creado.
         *
         * VALIDACIÓN PRINCIPAL:
         *  - idUsuario debe existir (referencia a User válida)
         *
         * NO VALIDAMOS:
         *  - Unicidad de nombre (puede haber "Trabajo" para múltiples usuarios)
         *  - Unicidad de color (puede haber varios tipos azules)
         *
         * FLUJO:
         *  1. Verificar que usuario existe
         *  2. Convertir DTO → Entity
         *  3. Guardar en BD
         *  4. Convertir Entity → DTO de respuesta
         */
        // GIVEN: Usuario existe
        when(userRepository.existsById(1)).thenReturn(true);
        when(typeMapper.toEntity(any(CreateTypeDTO.class))).thenReturn(testType);
        when(typeRepository.save(any(Type.class))).thenReturn(testType);
        when(typeMapper.toResponseDTO(any(Type.class))).thenReturn(typeResponseDTO);

        // WHEN: Creamos el tipo
        TypeResponseDTO result = typeService.create(createTypeDTO);

        // THEN: Verificaciones
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.nombre()).isEqualTo("Trabajo");
        assertThat(result.color()).isEqualTo("#326cc3");

        // Verificamos interacciones
        verify(userRepository, times(1)).existsById(1); // Validó usuario
        verify(typeRepository, times(1)).save(any(Type.class)); // Guardó tipo
    }

    @Test
    @DisplayName("❌ create() - Lanza ResourceNotFoundException cuando usuario no existe")
    void create_WhenUserNotExists_ThrowsResourceNotFoundException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que NO podemos crear un tipo para un usuario inexistente.
         *
         * ¿POR QUÉ ESTA VALIDACIÓN?
         *  - Mantiene integridad referencial (todo Type debe tener un User)
         *  - Evita "tipos huérfanos" sin dueño
         *  - La BD tiene FOREIGN KEY (id_usuario → usuarios.id)
         *
         * DIFERENCIA CON TaskService:
         *  - Task: idTipo es opcional (puede ser NULL)
         *  - Type: idUsuario es OBLIGATORIO (siempre debe tener dueño)
         *
         * ORDEN DE VALIDACIÓN:
         *  1. Service valida usuario ANTES de mapper/save (fail fast)
         *  2. BD también validaría (FK constraint), pero fallar antes es más eficiente
         */
        // GIVEN: Usuario no existe
        when(userRepository.existsById(999)).thenReturn(false);

        CreateTypeDTO dtoWithInvalidUser = new CreateTypeDTO(
                999,    // idUsuario inexistente
                "Tipo",
                "#ff0000"
        );

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.create(dtoWithInvalidUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario")
                .hasMessageContaining("999");

        // Verificamos que NO intentó guardar
        verify(typeRepository, never()).save(any(Type.class));
        verify(typeMapper, never()).toEntity(any(CreateTypeDTO.class));
    }

    // ========================================
    // TESTS UPDATE: Actualización de tipos
    // ========================================

    @Test
    @DisplayName("✅ update() - Actualiza tipo cuando hay cambios válidos")
    void update_WithValidChanges_UpdatesType() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que podemos actualizar nombre y color de un tipo.
         *
         * CASO DE USO REAL:
         *  Usuario quiere cambiar su tipo "Trabajo" de color azul a rojo.
         *
         * VALIDACIONES:
         *  1. Type existe (findById retorna Optional)
         *  2. UpdateDTO tiene cambios (tieneCambios() = true)
         *
         * NO VALIDAMOS:
         *  - Unicidad (pueden haber múltiples tipos con mismo nombre/color)
         */
        // GIVEN: Tipo existe + hay cambios
        UpdateTypeDTO updateDTO = new UpdateTypeDTO(
                "Nombre Actualizado",  // Nuevo nombre
                "#ff0000"              // Nuevo color (rojo)
        );

        when(typeRepository.findById(1)).thenReturn(Optional.of(testType));
        // updateEntityFromDTO es void, no retorna nada
        doAnswer(invocation -> {
            Type arg = invocation.getArgument(1);
            arg.setNombre("Nombre Actualizado");
            return null;
        }).when(typeMapper).updateEntityFromDTO(any(UpdateTypeDTO.class), any(Type.class));
        when(typeRepository.save(any(Type.class))).thenReturn(testType);
        when(typeMapper.toResponseDTO(any(Type.class))).thenReturn(typeResponseDTO);

        // WHEN: Actualizamos
        TypeResponseDTO result = typeService.update(1, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(typeRepository, times(1)).save(any(Type.class));
    }

    @Test
    @DisplayName("❌ update() - Lanza excepción cuando UpdateDTO no tiene cambios")
    void update_WhenNoChanges_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica la MISMA REGLA que UserService:
         * "No se puede actualizar sin cambios".
         *
         * ¿POR QUÉ?
         *  - Ahorra recursos (no ejecuta UPDATE innecesario)
         *  - Detecta bugs en frontend (ej: usuario hizo click sin modificar)
         *  - Comunica error claro al cliente
         *
         * IMPLEMENTACIÓN:
         *  - UpdateTypeDTO tiene constructor con todos campos opcionales
         *  - tieneCambios() retorna false si todos son null
         */
        // GIVEN: UpdateDTO vacío
        UpdateTypeDTO emptyDTO = new UpdateTypeDTO(null, null);

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.update(1, emptyDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se proporcionaron cambios");

        // Verificamos fail fast (no buscó el tipo)
        verify(typeRepository, never()).findById(anyInt());
    }

    // ========================================
    // TESTS DELETE: Eliminación de tipos
    // ========================================

    @Test
    @DisplayName("✅ delete() - Elimina tipo cuando no tiene tareas asociadas")
    void delete_WhenTypeHasNoTasks_DeletesType() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Happy path de eliminación: tipo sin tareas → se puede borrar.
         *
         * PROTECCIÓN DE INTEGRIDAD:
         *  - Verifica countByIdTipo antes de borrar
         *  - Si count = 0 → es seguro eliminar
         *  - Si count > 0 → rechaza (tareas quedarían huérfanas)
         *
         * ¿POR QUÉ NO USAR CASCADE?
         *  - Si borramos Type, ¿qué pasa con las Tasks de ese tipo?
         *  - Opción A: No permitir borrar (nuestra implementación) ← ELEGIDA
         *  - Opción B: Borrar Type + todas las Tasks (pérdida de datos)
         *  - Opción C: Desasignar Tasks (idTipo = NULL)
         *
         * JUSTIFICACIÓN:
         *  - Opción A es la más segura por defecto
         *  - Usuario debe decidir qué hacer con las tareas primero
         */
        // GIVEN: Tipo existe + no tiene tareas
        when(typeRepository.existsById(1)).thenReturn(true);
        when(taskRepository.countByIdTipo(1)).thenReturn(0L);
        doNothing().when(typeRepository).deleteById(1);

        // WHEN: Eliminamos
        typeService.delete(1);

        // THEN: Verificamos que se ejecutó delete
        verify(typeRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("❌ delete() - Lanza excepción cuando tipo tiene tareas asociadas")
    void delete_WhenTypeHasTasks_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica PROTECCIÓN CONTRA ELIMINACIÓN:
         * No podemos borrar un tipo que está siendo usado por tareas.
         *
         * ESCENARIO REAL:
         *  - Tipo "Trabajo" tiene 5 tareas asociadas
         *  - Usuario intenta borrar el tipo → RECHAZADO
         *  - Usuario debe primero reasignar o borrar esas 5 tareas
         *
         * MENSAJE DE ERROR:
         *  - Incluye el COUNT de tareas (ej: "5 tarea(s) asociada(s)")
         *  - Ayuda al usuario a entender el problema
         *
         * IMPLEMENTACIÓN:
         *  - countByIdTipo retorna 5 (tiene 5 tareas)
         *  - Service lanza BusinessRuleException
         *  - deleteById NUNCA se ejecuta
         */
        // GIVEN: Tipo existe + tiene 5 tareas
        when(typeRepository.existsById(1)).thenReturn(true);
        when(taskRepository.countByIdTipo(1)).thenReturn(5L);

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.delete(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede eliminar")
                .hasMessageContaining("tipo")
                .hasMessageContaining("5 tarea");

        // Verificamos que NO se eliminó
        verify(typeRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("❌ delete() - Lanza excepción cuando tipo no existe")
    void delete_WhenTypeNotExists_ThrowsResourceNotFoundException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que no podemos borrar un tipo inexistente.
         *
         * DIFERENCIA ENTRE EXCEPCIONES:
         *  - ResourceNotFoundException (404): "El tipo no existe"
         *  - BusinessRuleException (400): "El tipo existe pero tiene tareas"
         *
         * EJEMPLOS:
         *  - delete(999) donde 999 no existe → ResourceNotFoundException
         *  - delete(1) donde 1 tiene tareas → BusinessRuleException
         *
         * IMPORTANCIA DE LA DISTINCIÓN:
         *  - HTTP codes correctos (404 vs 400)
         *  - Mensajes claros para el cliente
         *  - Debugging más fácil
         */
        // GIVEN: Tipo no existe
        when(typeRepository.existsById(999)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tipo");

        verify(typeRepository, never()).deleteById(anyInt());
    }

    // ========================================
    // TESTS FIND: Consultas de tipos
    // ========================================

    @Test
    @DisplayName("✅ findById() - Retorna tipo cuando existe")
    void findById_WhenTypeExists_ReturnsType() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Test básico de búsqueda por ID.
         *
         * FLUJO:
         *  1. Repository busca por ID
         *  2. Si existe → mapper convierte a DTO
         *  3. Si no existe → ResourceNotFoundException
         */
        // GIVEN
        when(typeRepository.findById(1)).thenReturn(Optional.of(testType));
        when(typeMapper.toResponseDTO(any(Type.class))).thenReturn(typeResponseDTO);

        // WHEN
        TypeResponseDTO result = typeService.findById(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.nombre()).isEqualTo("Trabajo");
        verify(typeRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("❌ findById() - Lanza excepción cuando tipo no existe")
    void findById_WhenTypeNotExists_ThrowsResourceNotFoundException() {
        // GIVEN
        when(typeRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tipo")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("✅ findByIdUsuario() - Retorna lista de tipos de un usuario")
    void findByIdUsuario_WhenUserExists_ReturnsTypeList() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que podemos listar todos los tipos de un usuario específico.
         *
         * CASO DE USO:
         *  Usuario accede a su configuración → ve todos sus tipos personalizdos.
         *
         * VALIDACIÓN:
         *  - Usuario debe existir (existsById)
         *  - Si usuario no existe → ResourceNotFoundException
         *
         * FLUJO:
         *  1. Validar que usuario existe
         *  2. Buscar tipos donde id_usuario = X
         *  3. Mapear Type → TypeSummaryDTO
         */
        // GIVEN: Usuario existe + tiene 2 tipos
        Type type2 = new Type();
        type2.setId(2);
        type2.setIdUsuario(1);
        type2.setNombre("Personal");
        type2.setColor("#55a2d2");

        TypeSummaryDTO summary1 = new TypeSummaryDTO(1, "Trabajo", "#326cc3");
        TypeSummaryDTO summary2 = new TypeSummaryDTO(2, "Personal", "#55a2d2");
        List<Type> types = List.of(testType, type2);
        List<TypeSummaryDTO> expectedDTOs = List.of(summary1, summary2);

        when(userRepository.existsById(1)).thenReturn(true);
        when(typeRepository.findByIdUsuario(1)).thenReturn(types);
        when(typeMapper.toSummaryDTO(any(Type.class)))
                .thenReturn(summary1, summary2); // Llamadas secuenciales

        // WHEN: Buscamos tipos del usuario 1
        List<TypeSummaryDTO> result = typeService.findByIdUsuario(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nombre()).isEqualTo("Trabajo");
        assertThat(result.get(1).nombre()).isEqualTo("Personal");

        verify(userRepository, times(1)).existsById(1);
        verify(typeRepository, times(1)).findByIdUsuario(1);
    }

    @Test
    @DisplayName("❌ findByIdUsuario() - Lanza excepción cuando usuario no existe")
    void findByIdUsuario_WhenUserNotExists_ThrowsResourceNotFoundException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que NO podemos listar tipos de un usuario inexistente.
         *
         * ¿POR QUÉ ESTA VALIDACIÓN?
         *  - Mantener consistencia: si el usuario no existe, no tiene tipos
         *  - Evitar consultas innecesarias a typeRepository
         *  - Fail fast: detectar error antes de consultar BD
         */
        // GIVEN: Usuario no existe
        when(userRepository.existsById(999)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> typeService.findByIdUsuario(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario");

        // Verificamos que NO consultó typeRepository
        verify(typeRepository, never()).findByIdUsuario(anyInt());
    }

    @Test
    @DisplayName("✅ findAll() - Retorna lista de todos los tipos")
    void findAll_ReturnsTypeList() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Test simple de SELECT * FROM tipos.
         *
         * USO:
         *  - Admin listando todos los tipos del sistema
         *  - Reportes globales
         *
         * NOTA:
         *  - En producción, esto debería tener paginación
         *  - Para tests, asumimos dataset pequeño
         */
        // GIVEN
        TypeSummaryDTO summaryDTO = new TypeSummaryDTO(1, "Trabajo", "#326cc3");
        List<Type> types = List.of(testType);
        List<TypeSummaryDTO> expectedDTOs = List.of(summaryDTO);

        when(typeRepository.findAll()).thenReturn(types);
        when(typeMapper.toSummaryDTO(any(Type.class))).thenReturn(summaryDTO);

        // WHEN
        List<TypeSummaryDTO> result = typeService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Trabajo");
    }

    /*
     * COMPARATIVA: TaskService vs UserService vs TypeService
     * ======================================================
     *
     * TaskService:
     *  - ✅ Validación de fechas (fechaRealizacion >= fechaProgramacion)
     *  - ✅ Fecha automática al marcar completada
     *  - ✅ idTipo opcional (puede ser NULL)
     *
     * UserService:
     *  - ✅ Validación de unicidad (correo único)
     *  - ✅ Protección contra eliminación (tiene tareas)
     *  - ✅ UpdateDTO debe tener cambios
     *
     * TypeService:
     *  - ✅ Validación de usuario dueño (idUsuario debe existir)
     *  - ✅ Protección contra eliminación (tiene tareas)
     *  - ✅ UpdateDTO debe tener cambios
     *  - ❌ NO valida unicidad (pueden haber nombres duplicados)
     *
     * PATRONES COMUNES:
     *  1. ResourceNotFoundException para recursos inexistentes (404)
     *  2. BusinessRuleException para violaciones de reglas (400)
     *  3. Protección de integridad referencial
     *  4. Verificación de cambios en update
     *  5. Uso de MapStruct para conversiones
     *  6. @Transactional para métodos de modificación
     */
}
