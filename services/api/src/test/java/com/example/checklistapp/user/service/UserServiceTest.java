package com.example.checklistapp.user.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.mapper.UserMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserService
 *
 * PATRONES DE TESTING EN ESTE ARCHIVO:
 * ====================================
 * 1. **Validación de unicidad**: Verificar que no se pueden duplicar correos
 * 2. **Protección de integridad**: No eliminar usuarios con tareas asociadas
 * 3. **Validación de cambios**: UpdateDTO debe tener al menos un campo modificado
 * 4. **Cobertura de excepciones**: BusinessRuleException vs ResourceNotFoundException
 *
 * DIFERENCIAS CON TaskServiceTest:
 * ================================
 * - UserService tiene reglas de negocio más simples (no maneja fechas complejas)
 * - Enfoque en: validación de correo único + protección contra eliminación
 * - Menos interacciones con otros repositorios (solo UserRepository + TaskRepository)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // === MOCKS ===
    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserMapper userMapper;

    // === SYSTEM UNDER TEST (SUT) ===
    @InjectMocks
    private UserService userService;

    // === FIXTURE ===
    private User testUser;
    private CreateUserDTO createUserDTO;
    private UserResponseDTO userResponseDTO;

    /**
     * @BeforeEach: Inicializa datos de prueba ANTES de CADA test
     *
     * BENEFICIO: Evita duplicación de código y garantiza que cada test
     * parta de un estado limpio y predecible.
     */
    @BeforeEach
    void setUp() {
        // Usuario de prueba (simula un User de base de datos)
        testUser = new User();
        testUser.setId(1);
        testUser.setCorreo("test@example.com");
        testUser.setNombre("Usuario Test");

        // DTO para crear usuario (simula payload de HTTP POST)
        createUserDTO = new CreateUserDTO(
                "test@example.com",
                "Usuario Test"
        );

        // DTO de respuesta esperado (simula respuesta HTTP)
        userResponseDTO = new UserResponseDTO(
                1,
                "test@example.com",
                "Usuario Test",
                null // fechaCreación (no relevante para estos tests)
        );
    }

    // ========================================
    // TESTS CREATE: Creación de usuarios
    // ========================================

    @Test
    @DisplayName("✅ create() - Crea usuario exitosamente con correo único")
    void create_WithUniqueEmail_CreatesUser() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Este es el "HAPPY PATH": el escenario ideal donde todo funciona.
         *
         * GIVEN: Preparamos los mocks para simular:
         *  - El correo NO existe aún (único)
         *  - El mapper convierte DTO → Entity
         *  - El repository guarda y asigna ID
         *  - El mapper convierte Entity → DTO de respuesta
         *
         * WHEN: Ejecutamos el método a probar
         *
         * THEN: Verificamos:
         *  - El DTO retornado tiene los datos correctos
         *  - Se llamó a existsByCorreo para validar unicidad
         *  - Se llamó a save para persistir el usuario
         */
        // GIVEN: Correo único, mapper funciona, repository guarda
        when(userRepository.existsByCorreo("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(any(CreateUserDTO.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        // WHEN: Creamos el usuario
        UserResponseDTO result = userService.create(createUserDTO);

        // THEN: Verificaciones
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.correo()).isEqualTo("test@example.com");

        // Verificamos INTERACCIONES con los mocks
        verify(userRepository, times(1)).existsByCorreo("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ create() - Lanza BusinessRuleException cuando correo ya existe")
    void create_WhenEmailAlreadyExists_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Este test verifica una REGLA DE NEGOCIO: "el correo debe ser único".
         *
         * ESTRATEGIA:
         *  - Mock retornamos true (el correo YA existe)
         *  - Esperamos que se lance BusinessRuleException
         *  - Verificamos que NUNCA se llamó a save (no guardó duplicado)
         *
         * POR QUÉ IMPORTA:
         *  - Previene corrupción de datos (dos usuarios con mismo correo)
         *  - El servicio rechaza la solicitud antes de intentar guardar
         */
        // GIVEN: Correo duplicado
        when(userRepository.existsByCorreo("test@example.com")).thenReturn(true);

        // WHEN & THEN: Esperamos excepción
        assertThatThrownBy(() -> userService.create(createUserDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe un usuario")
                .hasMessageContaining("test@example.com");

        // Verificamos que NO se llamó a save (ahorro de recursos)
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toEntity(any(CreateUserDTO.class));
    }

    // ========================================
    // TESTS UPDATE: Actualización de usuarios
    // ========================================

    @Test
    @DisplayName("✅ update() - Actualiza usuario cuando hay cambios válidos")
    void update_WithValidChanges_UpdatesUser() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que podemos actualizar un usuario cambiando campos válidos.
         *
         * CASO DE USO REAL:
         *  Usuario quiere cambiar su nombre de "Juan" a "Juan Pérez"
         *
         * VALIDACIONES QUE ESTAMOS PROBANDO:
         *  1. El usuario existe (findById retorna Optional con User)
         *  2. El nuevo correo es único (existsByCorreo retorna false)
         *  3. UpdateDTO tiene cambios (tieneCambios() retorna true)
         */
        // GIVEN: Usuario existe + nuevo correo único + hay cambios
        UpdateUserDTO updateDTO = new UpdateUserDTO(
                "nuevo@example.com", // Nuevo correo
                "Nombre Actualizado" // Nuevo nombre
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByCorreo("nuevo@example.com")).thenReturn(false);
        // updateEntityFromDTO es void, no retorna nada
        doAnswer(invocation -> {
            User arg = invocation.getArgument(1);
            arg.setCorreo("nuevo@example.com");
            arg.setNombre("Nombre Actualizado");
            return null;
        }).when(userMapper).updateEntityFromDTO(any(UpdateUserDTO.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        // WHEN: Actualizamos
        UserResponseDTO result = userService.update(1, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ update() - Lanza excepción cuando UpdateDTO no tiene cambios")
    void update_WhenNoChanges_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica una REGLA DE NEGOCIO: "no se puede actualizar sin cambios".
         *
         * ¿POR QUÉ ESTA REGLA?
         *  - Ahorra recursos: no ejecuta UPDATE en BD si no hay nada que cambiar
         *  - Previene errores: detecta posibles bugs en el frontend
         *  - Claridad: el error comunica al cliente que su solicitud fue vacía
         *
         * IMPLEMENTACIÓN:
         *  - UpdateDTO.tieneCambios() retorna false (todos los campos son null)
         *  - Service lanza BusinessRuleException ANTES de buscar usuario
         */
        // GIVEN: UpdateDTO vacío (sin cambios)
        UpdateUserDTO emptyDTO = new UpdateUserDTO(null, null);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.update(1, emptyDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se proporcionaron cambios");

        // Verificamos que NUNCA buscó el usuario (falló rápido)
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    @DisplayName("❌ update() - Lanza excepción cuando nuevo correo ya está en uso")
    void update_WhenNewEmailAlreadyExists_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que NO podemos cambiar el correo a uno que ya usa otro usuario.
         *
         * ESCENARIO:
         *  - Usuario 1: juan@example.com
         *  - Usuario 2: maria@example.com
         *  - Usuario 1 intenta cambiar su correo a maria@example.com → RECHAZADO
         *
         * IMPLEMENTACIÓN DEL TEST:
         *  - Usuario existe (findById OK)
         *  - Nuevo correo ya pertenece a OTRO usuario (existsByCorreo = true)
         *  - Service lanza excepción
         */
        // GIVEN: Correo duplicado
        UpdateUserDTO updateDTO = new UpdateUserDTO(
                "otro@example.com", // Correo que ya existe
                null
        );

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        // Simula que "otro@example.com" ya lo usa alguien más
        when(userRepository.existsByCorreo("otro@example.com")).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.update(1, updateDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya existe un usuario");

        // Verificamos que NO guardó (abortó antes de save)
        verify(userRepository, never()).save(any(User.class));
    }

    // ========================================
    // TESTS DELETE: Eliminación de usuarios
    // ========================================

    @Test
    @DisplayName("✅ delete() - Elimina usuario cuando no tiene tareas asociadas")
    void delete_WhenUserHasNoTasks_DeletesUser() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Happy path de eliminación: usuario sin tareas → se puede borrar.
         *
         * VALIDACIONES:
         *  1. Usuario existe (existsById = true)
         *  2. No tiene tareas (countByIdUsuario = 0)
         *  3. Se ejecuta deleteById
         *
         * IMPORTANCIA:
         *  - Garantiza integridad referencial: no deja "huérfanos"
         *  - countByIdUsuario es una consulta eficiente (no trae todas las tareas)
         */
        // GIVEN: Usuario existe + no tiene tareas
        when(userRepository.existsById(1)).thenReturn(true);
        when(taskRepository.countByIdUsuario(1)).thenReturn(0L);
        doNothing().when(userRepository).deleteById(1);

        // WHEN: Eliminamos
        userService.delete(1);

        // THEN: Verificamos que se llamó a delete
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("❌ delete() - Lanza excepción cuando usuario tiene tareas asociadas")
    void delete_WhenUserHasTasks_ThrowsBusinessRuleException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica PROTECCIÓN DE INTEGRIDAD: no se puede borrar un usuario
         * que tiene tareas asociadas (¿qué pasaría con esas tareas?).
         *
         * ALTERNATIVAS DE DISEÑO:
         *  Opción A: No permitir borrar (Nuestra implementación) ← ELEGIDA
         *  Opción B: Cascade delete (borrar usuario + todas sus tareas)
         *  Opción C: Desasignar tareas (idUsuario = NULL)
         *
         * JUSTIFICACIÓN DE OPCIÓN A:
         *  - Previerte pérdida de datos accidental
         *  - Obliga al usuario a decidir qué hacer con las tareas
         *  - Es más seguro por defecto
         *
         * IMPLEMENTACIÓN:
         *  - countByIdUsuario retorna 3 (tiene 3 tareas)
         *  - Service lanza BusinessRuleException con mensaje descriptivo
         */
        // GIVEN: Usuario existe + tiene 3 tareas asociadas
        when(userRepository.existsById(1)).thenReturn(true);
        when(taskRepository.countByIdUsuario(1)).thenReturn(3L);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.delete(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede eliminar")
                .hasMessageContaining("3 tarea");

        // Verificamos que NO se llamó a deleteById
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("❌ delete() - Lanza excepción cuando usuario no existe")
    void delete_WhenUserNotExists_ThrowsResourceNotFoundException() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Verifica que no podemos "borrar algo que no existe".
         *
         * DIFERENCIA ENTRE EXCEPCIONES:
         *  - ResourceNotFoundException: "El recurso no existe" (404)
         *  - BusinessRuleException: "El recurso existe pero viola regla" (400)
         *
         * EJEMPLO:
         *  - delete(999) donde 999 no existe → ResourceNotFoundException
         *  - delete(1) donde 1 tiene tareas → BusinessRuleException
         */
        // GIVEN: Usuario no existe
        when(userRepository.existsById(999)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario");

        verify(userRepository, never()).deleteById(anyInt());
    }

    // ========================================
    // TESTS FIND: Consultas de usuarios
    // ========================================

    @Test
    @DisplayName("✅ findAll() - Retorna lista de todos los usuarios")
    void findAll_ReturnsUserList() {
        /**
         * EXPLICACIÓN DEL TEST:
         * =====================
         * Test simple de consulta (SELECT * FROM usuarios).
         *
         * FLUJO:
         *  1. Repository retorna lista de User entities
         *  2. Mapper convierte cada User → UserSummaryDTO
         *  3. Service retorna lista de DTOs
         *
         * PATRÓN STREAM:
         *  - El service usa .stream().map(mapper::toSummaryDTO).toList()
         *  - Aquí mockeamos directamente la respuesta final
         */
        // GIVEN
        // UserSummaryDTO: id, nombre, correo
        UserSummaryDTO summaryDTO = new UserSummaryDTO(1, "Usuario Test", "test@example.com");
        List<User> users = List.of(testUser);
        List<UserSummaryDTO> expectedDTOs = List.of(summaryDTO);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toSummaryDTO(any(User.class))).thenReturn(summaryDTO);

        // WHEN
        List<UserSummaryDTO> result = userService.findAll();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).correo()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("✅ findById() - Retorna usuario cuando existe")
    void findById_WhenUserExists_ReturnsUser() {
        // GIVEN
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        // WHEN
        UserResponseDTO result = userService.findById(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("❌ findById() - Lanza excepción cuando usuario no existe")
    void findById_WhenUserNotExists_ThrowsResourceNotFoundException() {
        // GIVEN
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> userService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /*
     * RESUMEN DE CONCEPTOS APRENDIDOS:
     * ================================
     * 1. **Aislamiento**: Tests unitarios puros sin contexto Spring
     * 2. **Given-When-Then**: Estructura clara y legible
     * 3. **Happy Path + Edge Cases**: Cubrimos éxito y fallos esperados
     * 4. **Verificación doble**: AssertJ (resultados) + Mockito (interacciones)
     * 5. **Reglas de negocio**: Validamos unicidad, integridad, protecciones
     * 6. **Messaging**: Excepciones con mensajes descriptivos
     * 7. **never()**: Verificamos que NO se ejecutó código innecesario
     */
}
