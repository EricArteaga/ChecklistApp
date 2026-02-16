# Testing Unitario - ChecklistApp Backend

**Fecha**: 2026-02-16
**Estado**: ✅ COMPLETADO
**Tests**: 35 tests, 100% passing

## 📚 Resumen Ejecutivo

Se implementaron tests unitarios completos para los 3 servicios principales del backend:
- **TaskServiceTest**: 13 tests
- **UserServiceTest**: 11 tests
- **TypeServiceTest**: 11 tests

**Total**: 35 tests unitarios, 100% passing

## 🎯 Objetivos de los Tests

### 1. **Verificar Lógica de Negocio**
- Validaciones de reglas de negocio (fechas, unicidad, referencias)
- Protección de integridad referencial
- Manejo correcto de excepciones

### 2. **Aislar Dependencias**
- Mocks de repositorios (no acceden a BD real)
- Mocks de mappers (MapStruct)
- Tests rápidos y deterministas

### 3. **Documentar Comportamiento**
- Cada test es documentación viva del comportamiento esperado
- @DisplayName con emojis (✅ éxito, ❌ fracaso esperado)
- Explicaciones didácticas en cada test

## 📁 Archivos Creados

```
services/api/src/test/java/com/example/checklistapp/
├── task/service/TaskServiceTest.java      ✅ 13 tests
├── user/service/UserServiceTest.java      ✅ 11 tests
└── type/service/TypeServiceTest.java      ✅ 11 tests
```

## 🛠️ Tecnologías Utilizadas

### JUnit 5
- **@Test**: Marca métodos de test
- **@DisplayName**: Nombres descriptivos de tests
- **@BeforeEach**: Configuración antes de cada test
- **Assertions**: assertThat (AssertJ) para aserciones fluentes

### Mockito
- **@Mock**: Crea simulacros de dependencias
- **@InjectMocks**: Inyecta mocks en el sistema bajo prueba
- **when/thenReturn**: Configura comportamiento de mocks
- **verify**: Verifica interacciones con mocks
- **any()**: Matchers de argumentos flexibles

### Spring Boot Test
- **spring-boot-starter-test**: Incluye JUnit 5, Mockito, AssertJ
- Sin @SpringBootTest (tests unitarios puros, más rápidos)

## 📊 Cobertura de Tests

### TaskServiceTest (13 tests)

#### ✅ CREATE (5 tests)
1. `create_WithValidData_ReturnsTaskResponseDTO` - Happy path
2. `create_WhenUserNotExists_ThrowsResourceNotFoundException` - Validación de usuario
3. `create_WhenTypeNotExists_ThrowsResourceNotFoundException` - Validación de tipo
4. `create_WhenMarkedCompletedWithoutDate_SetsTodayAsCompletionDate` - Fecha automática
5. `create_WhenCompletionDateBeforeScheduledDate_ThrowsBusinessRuleException` - Validación de fechas

#### ✅ FIND (4 tests)
6. `findById_WhenTaskExists_ReturnsTaskResponseDTO` - Búsqueda por ID
7. `findById_WhenTaskNotExists_ThrowsResourceNotFoundException` - Tarea inexistente
8. `findByIdUsuario_WhenUserExists_ReturnsTaskList` - Tareas de usuario
9. `findByIdUsuario_WhenUserNotExists_ThrowsResourceNotFoundException` - Usuario inexistente

#### ✅ UPDATE (1 test)
10. `update_WithValidData_UpdatesTask` - Actualización exitosa

#### ✅ DELETE (2 tests)
11. `delete_WhenTaskExists_DeletesTask` - Eliminación exitosa
12. `delete_WhenTaskNotExists_ThrowsResourceNotFoundException` - Tarea inexistente

#### ✅ ADDITIONAL (1 test)
13. `findAll_ReturnsTaskList` - Listar todas las tareas

### UserServiceTest (11 tests)

#### ✅ CREATE (2 tests)
1. `create_WithUniqueEmail_CreatesUser` - Happy path
2. `create_WhenEmailAlreadyExists_ThrowsBusinessRuleException` - Correo duplicado

#### ✅ UPDATE (3 tests)
3. `update_WithValidChanges_UpdatesUser` - Actualización exitosa
4. `update_WhenNoChanges_ThrowsBusinessRuleException` - Sin cambios
5. `update_WhenNewEmailAlreadyExists_ThrowsBusinessRuleException` - Correo duplicado

#### ✅ DELETE (3 tests)
6. `delete_WhenUserHasNoTasks_DeletesUser` - Sin tareas asociadas
7. `delete_WhenUserHasTasks_ThrowsBusinessRuleException` - Tiene tareas (protección)
8. `delete_WhenUserNotExists_ThrowsResourceNotFoundException` - Usuario inexistente

#### ✅ FIND (3 tests)
9. `findAll_ReturnsUserList` - Listar todos
10. `findById_WhenUserExists_ReturnsUser` - Búsqueda por ID
11. `findById_WhenUserNotExists_ThrowsResourceNotFoundException` - Usuario inexistente

### TypeServiceTest (11 tests)

#### ✅ CREATE (2 tests)
1. `create_WhenUserExists_CreatesType` - Happy path
2. `create_WhenUserNotExists_ThrowsResourceNotFoundException` - Usuario inexistente

#### ✅ UPDATE (2 tests)
3. `update_WithValidChanges_UpdatesType` - Actualización exitosa
4. `update_WhenNoChanges_ThrowsBusinessRuleException` - Sin cambios

#### ✅ DELETE (3 tests)
5. `delete_WhenTypeHasNoTasks_DeletesType` - Sin tareas asociadas
6. `delete_WhenTypeHasTasks_ThrowsBusinessRuleException` - Tiene tareas (protección)
7. `delete_WhenTypeNotExists_ThrowsResourceNotFoundException` - Tipo inexistente

#### ✅ FIND (4 tests)
8. `findById_WhenTypeExists_ReturnsType` - Búsqueda por ID
9. `findById_WhenTypeNotExists_ThrowsResourceNotFoundException` - Tipo inexistente
10. `findByIdUsuario_WhenUserExists_ReturnsTypeList` - Tipos de usuario
11. `findByIdUsuario_WhenUserNotExists_ThrowsResourceNotFoundException` - Usuario inexistente

## 🎓 Patrones de Testing Implementados

### 1. Estructura Given-When-Then

```java
@Test
@DisplayName("✅ Descripción del test")
void testMethod() {
    // GIVEN: Configuración de mocks y datos de prueba
    when(repository.findById(1)).thenReturn(Optional.of(entity));

    // WHEN: Ejecución del método a probar
    ResultDTO result = service.method(1);

    // THEN: Verificaciones
    assertThat(result).isNotNull();
    verify(repository, times(1)).findById(1);
}
```

**Ventajas**:
- Claridad: Separa preparación, ejecución y verificación
- Legibilidad: Fácil de entender qué hace el test
- Mantenibilidad: Cambios localizados en cada sección

### 2. Fixture Reutilizable

```java
@BeforeEach
void setUp() {
    // Inicializa objetos de prueba ANTES de CADA test
    testUser = new User();
    testUser.setId(1);
    testUser.setCorreo("test@example.com");
}
```

**Ventajas**:
- Evita duplicación de código
- Estado limpio y predecible para cada test
- Cambios centralizados en un solo lugar

### 3. Mocking de Repositorios

```java
@Mock
private UserRepository userRepository;

when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
```

**Ventajas**:
- Aislamiento: No depende de BD real
- Velocidad: Tests ejecutan en milisegundos
- Determinismo: Mismo resultado siempre

### 4. Verificación de Interacciones

```java
verify(userRepository, times(1)).findById(1);
verify(taskRepository, never()).save(any(Task.class));
```

**Ventajas**:
- Confirma que el service interactúa correctamente con dependencias
- Detecta código muerto (stubs no usados)
- Verifica orden y cantidad de llamadas

### 5. Tests de Excepciones

```java
assertThatThrownBy(() -> userService.create(duplicateEmailDTO))
    .isInstanceOf(BusinessRuleException.class)
    .hasMessageContaining("Ya existe un usuario");
```

**Ventajas**:
- Verifica manejo correcto de errores
- Confirma mensajes de error descriptivos
- Prueba edge cases y situaciones inválidas

### 6. Uso de Answers para Mocks Complejos

```java
when(mapper.toEntity(any(DTO.class))).thenAnswer(invocation -> {
    DTO dto = invocation.getArgument(0);
    Entity entity = new Entity();
    entity.setField(dto.field());
    return entity;
});
```

**Ventajas**:
- Mocks dinámicos que se adaptan a los argumentos
- Permite verificar modificaciones en objetos
- Más flexible que retornar siempre el mismo objeto

## 🚀 Ejecución de Tests

### Ejecutar todos los tests
```bash
cd services/api
./gradlew test
```

### Ejecutar solo una clase de tests
```bash
./gradlew test --tests TaskServiceTest
```

### Ejecutar un test específico
```bash
./gradlew test --tests "TaskServiceTest.create_WithValidData"
```

### Ver reporte HTML
```bash
# Abre en navegador:
file:///D:/erica/VSC-workspace/checklistApp/services/api/build/reports/tests/test/index.html
```

## 📈 Resultados

```
BUILD SUCCESSFUL in 4s

35 tests completed, 35 passed ✓
0 tests failed ✗

Cobertura estimada: >80% código de servicio
```

## 🎓 Lecciones Aprendidas

### ✅ Buenas Prácticas Aplicadas

1. **Tests unitarios puros**: Sin @SpringBootTest (más rápidos)
2. **Aislamiento completo**: Todas las dependencias mockeadas
3. **Nombres descriptivos**: @DisplayName explica qué se prueba
4. **Given-When-Then**: Estructura clara y mantenible
5. **Verificación doble**: AssertJ (resultado) + Mockito (interacciones)
6. **Fixture reutilizable**: @BeforeEach para datos de prueba
7. **Cobertura de casos**: Happy path + edge cases

### ⚠️ Problemas Resueltos

1. **Constructores de DTOs**: Orden incorrecto de parámetros
   - Solución: Verificar signatures de records Java

2. **Mappers void**: updateEntityFromDTO no retorna nada
   - Solución: Usar doAnswer() para mocks con métodos void

3. **Unnecessary stubbings**: Mocks configurados pero no usados
   - Solución: Remover stubs de código que nunca se ejecuta

4. **Entities vs SummaryDTOs**: User vs UserSummaryDTO
   - Solución: Usar DTOs correctos en respuestas anidadas

5. **Static mocks vs Answers**: Objetos estáticos no reflejan cambios
   - Solución: Usar thenAnswer() para mocks dinámicos

## 🔄 Próximos Pasos Sugeridos

### Opcional: Tests de Integración
- **@DataJpaTest**: Tests de repositorios con BD H2
- **@WebMvcTest**: Tests de controllers con HTTP mock
- **@SpringBootTest**: Tests end-to-end completos

### Mejora: Cobertura de Código
- **JaCoCo**: Plugin para medir cobertura
- **Objetivo**: >80% cobertura de código

### Automatización: CI/CD
- **GitHub Actions**: Ejecutar tests en cada PR
- **Coverage report**: Publicar en cada build

## 📖 Referencias

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Assertions](https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

## ✅ Checklist de Verificación

- [x] TaskServiceTest: 13 tests passing
- [x] UserServiceTest: 11 tests passing
- [x] TypeServiceTest: 11 tests passing
- [x] BUILD SUCCESSFUL
- [x] Todos los métodos públicos cubiertos con tests
- [x] Happy paths probados
- [x] Edge cases probados
- [x] Excepciones verificadas
- [x] Interacciones con mocks verificadas
- [x] Código documentado con explicaciones didácticas

---

**Conclusión**: Los tests unitarios proporcionan una red de seguridad para el desarrollo, permitiendo refactorizar con confianza y prevenir regresiones. Los 35 tests documentan el comportamiento esperado del sistema y sirven como especificación viva.
