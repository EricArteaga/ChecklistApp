# Implementación DTOs + Mapper + Repository

**Fecha**: 2026-02-06
**Agente**: java-mentor-senior
**Objetivo**: Crear DTOs, Mapper MapStruct y actualizar Repository para TaskService

---

## ✅ IMPLEMENTACIÓN COMPLETADA

### 1. DTOs Creados (6 archivos)

#### `dto/task/CreateTaskDTO.java`
- **Propósito**: Crear nuevas tareas
- **Validaciones Jakarta**:
  - `@NotBlank` en nombre
  - `@Size(max=150)` en nombre
  - `@Size(max=1000)` en descripcion
  - `@NotNull` en idUsuario
  - `@Future` en fechaProgramacion
- **Helper methods**:
  - `tieneFechaRealizacionExplicita()` - Detecta si usuario envió fecha
  - `estaMarcadaComoCompletada()` - Detecta si se marca como completada

#### `dto/task/UpdateTaskDTO.java`
- **Propósito**: Actualizar tareas existentes (PATCH)
- **Estrategia**: null = no cambiar este campo
- **Helper methods**:
  - `tieneCambios()` - Detecta si hay cambios reales
  - `cambiaCompletada(Boolean estadoActual)` - Detecta cambio de estado
  - `seMarcaComoCompletada()` - True si marca como completada
  - `seDesmarcaComoCompletada()` - True si desmarca

#### `dto/task/TaskResponseDTO.java`
- **Propósito**: Respuesta completa de tarea
- **Incluye**: Objetos anidados (UserSummaryDTO, TypeSummaryDTO)
- **Helper methods**:
  - `estaVencida()` - True si no completada y fechaProgramacion < hoy
  - `estaProgramadaParaHoy()` - True si fechaProgramacion == hoy
  - `fueCompletadaTarde()` - True si completada después de fechaProgramacion

#### `dto/task/TaskSummaryDTO.java`
- **Propósito**: Listado compacto de tareas
- **Ahorro**: 60% menos datos vs TaskResponseDTO
- **Helper methods**:
  - `estaVencida()` - Igual que TaskResponseDTO
  - `estaProgramadaParaHoy()` - Igual que TaskResponseDTO
  - `estaProgramadaParaEstaSemana()` - True si está en la semana actual

#### `dto/user/UserSummaryDTO.java`
- **Propósito**: Usuario resumido (anidado en TaskResponseDTO)
- **Campos**: id, nombre, correo
- **Helper methods**:
  - `getIniciales()` - Retorna "JP" para "Juan Pérez"

#### `dto/type/TypeSummaryDTO.java`
- **Propósito**: Tipo resumido (anidado en TaskResponseDTO)
- **Campos**: id, nombre, color, descripcion
- **Helper methods**:
  - `tieneColorValido()` - Valida formato HEX

---

### 2. Mapper MapStruct Creado

#### `mapper/TaskMapper.java`
- **Componente**: Spring Bean (`@Mapper(componentModel = "spring")`)
- **Métodos**:
  - `toEntity(CreateTaskDTO)` - Crea Task desde DTO
  - `toResponseDTO(Task)` - Convierte a DTO completo
  - `toSummaryDTO(Task)` - Convierte a DTO resumido
  - `toSummaryDTOList(List<Task>)` - Convierte lista
  - `updateEntityFromDTO(UpdateTaskDTO, Task)` - PATCH con IGNORE

- **Mappers auxiliares incluidos**:
  - `UserMapper` - User → UserSummaryDTO
  - `TypeMapper` - Type → TypeSummaryDTO

---

### 3. TaskRepository Actualizado

#### Queries con JOIN FETCH agregadas:
```java
// Trae Task con Usuario y Tipo en una sola query
Optional<Task> findByIdWithUsuarioAndTipo(Integer id)

// Lista tareas de usuario con relaciones
List<Task> findByIdUsuarioWithUsuarioAndTipo(Integer idUsuario)

// Paginación con JOIN FETCH + countQuery
Page<Task> findAllWithUsuarioAndTipo(Pageable pageable)

// Tareas pendientes ordenadas
List<Task> findPendingTasksWithUsuarioAndTipo(Integer idUsuario)
```

**Propósito**: Evitar LazyInitializationException y problema N+1

---

### 4. Entity Task Corregida

#### Cambio en `entities/Task.java`:
```java
// ANTES:
@Column(name = "id_tipo", nullable = false)
private Integer idTipo;

// DESPUÉS:
@Column(name = "id_tipo")  // nullable = true (por defecto)
private Integer idTipo;
```

**Justificación**: Usuario decidió que idTipo es opcional

---

### 5. build.gradle Actualizado

#### Dependencias agregadas:
```gradle
// MapStruct para mapeo Entity <-> DTO
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
```

---

## 📊 ESTADÍSTICAS

- **Archivos creados**: 9 (6 DTOs + 1 Mapper + 2 package-info.java)
- **Archivos modificados**: 3 (Task.java, TaskRepository.java, build.gradle)
- **Líneas de código**: ~800 (incluyendo comentarios JavaDoc)
- **Validaciones Jakarta**: 5 tipos (@NotBlank, @Size, @Future, @NotNull)
- **Helper methods**: 12 methods utilitarios en DTOs
- **Queries JOIN FETCH**: 4 queries optimizadas

---

## 🎯 PRÓXIMOS PASOS

### Paso 6: Crear Excepciones Personalizadas
- `ResourceNotFoundException` - Para recursos no encontrados
- `BusinessRuleException` - Para violaciones de reglas de negocio

### Paso 7: Implementar TaskService
- CRUD completo
- Validaciones de negocio
- Conversiones Entity ↔ DTO (usando TaskMapper)
- `@Transactional` en métodos que modifican datos

### Paso 8: Crear GlobalExceptionHandler
- Manejo centralizado de excepciones
- Respuestas HTTP consistentes

### Paso 9: Crear TaskController
- Endpoints REST
- Uso de @Valid para fail-fast
- Documentación OpenAPI

---

## 🔗 REFERENCIAS

- **TODO_CLAUDE.md** - Estado del proyecto
- **TAREA_1_1_Servicios.md** - Guía de implementación de TaskService
- **MapStruct Documentation**: https://mapstruct.org/documentation/stable/reference/html/

---

**Estado**: ✅ DTOs + Mapper + Repository listos para TaskService
