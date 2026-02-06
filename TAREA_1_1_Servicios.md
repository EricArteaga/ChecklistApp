# Tarea 1.1: Implementar Servicios Completos - Enfoque y Contexto

## Objetivo de la Tarea

Transformar los servicios mock actuales en una **capa de negocio funcional** que orqueste operaciones entre controladores y repositorios, aplicando lógica de dominio, validaciones y reglas de negocio.

---

## Estado Actual vs Objetivo

### Actual
```
Controller → Service Mock → Respuesta hardcoded
❌ Sin persistencia
❌ Sin validaciones
❌ Sin lógica de negocio
```

### Objetivo
```
Controller → Service → Repository → Base de Datos
✅ Persistencia real
✅ Validaciones de negocio
✅ Lógica de dominio
✅ Manejo de errores
```

---

## Arquitectura de Capas

### Responsabilidad de Cada Capa

| Capa | Responsabilidad | NO debe hacer |
|------|----------------|---------------|
| **Controller** | Recibir HTTP, validar sintaxis, devolver HTTP | Lógica de negocio |
| **Service** | Lógica de negocio, orquestación, transacciones | Acceder directamente a BD |
| **Repository** | Abrir/cerrar conexiones, ejecutar queries | Reglas de negocio |

### Por qué Service es crítico
- **Separación de concerns**: El controller no debe saber cómo se calculan los datos
- **Reutilización**: Mismo service usado por REST API, GraphQL, o scheduled jobs
- **Testing**: Lógica aislada facilita unit tests
- **Transacciones**: `@Transactional` garantiza atomicidad en operaciones complejas

---

## Decisiones Aritectónicas Previas

### 1. **ChecklistService vs TaskService**
- Actualmente existe `ChecklistService` pero el dominio es `Task`
- **Decisión needed**: Renombrar a `TaskService` o mantener `ChecklistService`
- **Impacto**: Afecta naming de todos los componentes

### 2. **Relación Task ↔ Type**
- Un Task pertenece a un Type (categoría)
- **Pregunta**: ¿Service debe validar que Type existe antes de crear Task?
- **Decisión**: Sí, lanzar excepción si Type no existe

### 3. **Relación Task ↔ User**
- Un Task pertenece a un User
- **Pregunta**: ¿Service debe validar que User existe?
- **Decisión**: Sí, validar autorización y existencia

### 4. **DTOs vs Entidades**
- Entidades JPA = representación BD
- DTOs = representación API
- **Service debe convertir**: Entity ←→ DTO
- **Por qué**: Desacoplar esquema BD de contrato API

---

## Servicios Necesarios

### Servicio Principal: TaskService
**Ubicación**: `services/api/src/main/java/com/example/checklistapp/services/TaskService.java`

**Responsabilidades**:
1. CRUD básico de Task
2. Validaciones de negocio (fechas, estados, referencias)
3. Consultas especiales (por usuario, por rango, pendientes)
4. Toggle de estado completada
5. Actualización automática de `fechaRealizacion`

**Dependencias**:
- `TaskRepository` (ya existe con queries personalizadas)
- `UserRepository` (validar usuario existe)
- `TypeRepository` (validar tipo existe)

### Servicios Opcionales (Futuro)
- `UserService`: Gestión de usuarios
- `TypeService`: Gestión de categorías
- `AuthService`: Autenticación (Tarea 1.2)

---

## Lógica de Negocio a Implementar

### Reglas de Dominio

| Regla | Descripción | Implementación |
|-------|-------------|----------------|
| **Fecha creación** | Automática al crear | `@PrePersist` en Entity |
| **Fecha realización** | Se setea solo si `completada = true` | En método `toggleCompletion` |
| **Usuario existe** | Validar antes de crear Task | `userService.existsById()` |
| **Tipo existe** | Validar antes de crear Task | `typeService.existsById()` |
| **No eliminar usuario con tareas** | Proteger integridad referencial | Cascade en DB o validación |
| **Fecha programación** | No puede ser pasada (opcional) | Validación en Service |

### Transacciones
```java
@Transactional // Todas las operaciones son atómicas
public TaskDTO createTask(CreateTaskDTO dto) {
    // Si algo falla, todo se rollback
}
```

---

## Secuencia de Implementación

### PASO 1: Crear estructura de paquetes
```
services/api/src/main/java/com/example/checklistapp/
├── dto/              (DTOs ya creados en guía anterior)
├── services/         (nuevo paquete)
└── exceptions/       (excepciones de dominio)
```

**Por qué**: Organizar por responsabilidad, no por capa

### PASO 2: Crear DTOs
- `TaskDTO` - Para respuestas (READ)
- `CreateTaskDTO` - Para crear (CREATE)
- `UpdateTaskDTO` - Para actualizar (UPDATE)
- `_Por qué_: Separar entrada/salida permite diferentes validaciones`

### PASO 3: Implementar TaskService
**Métodos requeridos**:

**CRUD Básico**:
```java
TaskDTO createTask(CreateTaskDTO dto)
TaskDTO updateTask(Integer id, CreateTaskDTO dto)
TaskDTO getTaskById(Integer id)
List<TaskDTO> getAllTasks()
void deleteTask(Integer id)
```

**Operaciones Especiales**:
```java
TaskDTO toggleTaskCompletion(Integer id)
List<TaskDTO> getTasksByUser(Integer userId)
List<TaskDTO> getPendingTasks(Integer userId)
List<TaskDTO> getCompletedTasks(Integer userId)
```

**Consultas por Fecha**:
```java
List<TaskDTO> getTasksByDateRange(Integer userId, LocalDate start, LocalDate end)
Long getCompletedTasksCount(Integer userId, LocalDate date) // Para heatmap
```

**Por qué este orden**: Primero lo básico, luego consultas específicas

### PASO 4: Manejo de Errores
- Crear excepciones: `ResourceNotFoundException`, `BusinessRuleException`
- Service lanza excepciones → Controller las maneja
- `_Por qué_: Separar código de happy path de error handling`

### PASO 5: Conversiones Entity ↔ DTO
```java
private TaskDTO entityToDTO(Task entity) { ... }
private Task dtoToEntity(CreateTaskDTO dto) { ... }
```
- `_Por qué_: Mantener service limpio de lógica de mapeo`

---

## Dependencias y Orden

### Grafo de Dependencias
```
TaskService
    ├→ TaskRepository (ya existe ✓)
    ├→ UserRepository (ya existe ✓)
    └→ TypeRepository (ya existe ✓)

TaskRepository
    └→ Entidad Task (ya existe ✓)
```

### Orden de Implementación
1. **DTOs** (no dependen de nada)
2. **Excepciones** (no dependen de nada)
3. **Service** (depende de repositorios que ya existen)
4. **Controller** (Tarea 1.3, depende de Service)

---

## Casos de Uso a Cubrir

### UC1: Crear Tarea
```
Usuario → POST /api/tasks { idUsuario: 1, idTipo: 2, nombre: "..."}
  ↓
Controller recibe JSON → lo convierte a CreateTaskDTO
  ↓
Controller llama a taskService.createTask(dto)
  ↓
Service valida: ¿existe usuario 1? → userRepository.findById(1)
Service valida: ¿existe tipo 2? → typeRepository.findById(2)
  ↓
Service crea Entity Task con los datos
Service llama a taskRepository.save(task)
  ↓
Service convierte Entity a TaskDTO
  ↓
Service devuelve TaskDTO al Controller
  ↓
Controller devuelve TaskDTO como JSON + HTTP 201
```

### UC2: Marcar Tarea como Completada
```
Usuario → PATCH /api/tasks/5/toggle
  ↓
Controller llama a taskService.toggleTaskCompletion(5)
  ↓
Service busca task 5 → taskRepository.findById(5)
Service hace: task.setCompletada(!task.getCompletada())
Si ahora es true → task.setFechaRealizacion(LocalDate.now())
Si ahora es false → task.setFechaRealizacion(null)
  ↓
Service guarda → taskRepository.save(task)
  ↓
Service devuelve TaskDTO actualizado
```

### UC3: Obtener Tareas Pendientes de Usuario
```
Usuario → GET /api/tasks/user/1/pending
  ↓
Controller llama a taskService.getPendingTasks(1)
  ↓
Service llama a taskRepository.findByIdUsuarioAndCompletada(1, false)
  ↓
Repository devuelve List<Task>
Service convierte cada Task a TaskDTO
Service devuelve List<TaskDTO>
```

---

## Validaciones en Service vs Controller

### Validaciones en Controller (sintaxis)
```java
@RequestParam @NotBlank String nombre  // ¿No está vacío?
@RequestParam @Email String email       // ¿Es email válido?
```

### Validaciones en Service (semántica)
```java
if (!userRepository.existsById(idUsuario)) {
    throw new ResourceNotFoundException("Usuario no existe");
}
if (task.getFechaProgramacion().isBefore(LocalDate.now())) {
    throw new BusinessRuleException("Fecha programada no puede ser pasada");
}
```

**Por qué esta separación**:
- Controller valida **formato** (es un email?)
- Service valida **negocio** (existe ese usuario?)

---

## Testing Strategy

### Unit Tests de Service
```java
@Test void crearTarea_conUsuarioInexistente_lanzaExcepcion() {
    when(userRepository.existsById(999)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class,
                 () -> taskService.createTask(dto));
}
```

### Integration Tests
```java
@Test void crearTarea_guardaCorrectamenteEnBD() {
    TaskDTO result = taskService.createTask(dto);
    assertNotNull(result.getId());
    assertTrue(taskRepository.existsById(result.getId()));
}
```

---

## Checklist de Completitud

### Estructura
- [ ] Paquete `services/` creado
- [ ] Paquete `dto/` creado
- [ ] Paquete `exceptions/` creado

### DTOs
- [ ] `TaskDTO` con todos los campos
- [ ] `CreateTaskDTO` con validaciones
- [ ] `UpdateTaskDTO` (si se necesita diferente a Create)

### Excepciones
- [ ] `ResourceNotFoundException`
- [ ] `BusinessRuleException`

### Service
- [ ] CRUD completo implementado
- [ ] Validaciones de referencias (user, type)
- [ ] Métodos de conversión Entity ↔ DTO
- [ ] `@Transactional` en métodos que modifican datos
- [ ] Toggle de completada con actualización de fecha

### Repositorios
- [ ] `TaskRepository` ya tiene queries necesarias ✓
- [ ] Verificar que `UserRepository` y `TypeRepository` tienen métodos básicos

---

## Riesgos y Consideraciones

| Riesgo | Mitigación |
|--------|------------|
| **LazyInitializationException** | Usar `@EntityGraph` o `JOIN FETCH` en queries |
| **N+1 queries** | Recuperar entidades relacionadas en una sola query |
| **Transacciones largas** | Mantener operaciones cortas y específicas |
| **Excepciones genéricas** | Crear excepciones de dominio específicas |

---

## Próximos Pasos

Una vez completada esta tarea:

1. **Tarea 1.2**: Autenticación (depende de Service estar funcional)
2. **Tarea 1.3**: Controller (consume Service)
3. **Tarea 1.4**: DTOs mejorados y validaciones globales

---

## Archivos a Modificar/Crear

### Modificar
- `services/api/src/main/java/com/example/checklistapp/checklist/ChecklistService.java`
  → Renombrar a `TaskService.java` y mover a paquete `services/`

### Crear
- `services/api/src/main/java/com/example/checklistapp/services/TaskService.java`
- `services/api/src/main/java/com/example/checklistapp/dto/TaskDTO.java`
- `services/api/src/main/java/com/example/checklistapp/dto/CreateTaskDTO.java`
- `services/api/src/main/java/com/example/checklistapp/exceptions/ResourceNotFoundException.java`

### Ya existen (no tocar)
- `services/api/src/main/java/com/example/checklistapp/repositories/TaskRepository.java` ✓
- `services/api/src/main/java/com/example/checklistapp/repositories/UserRepository.java` ✓
- `services/api/src/main/java/com/example/checklistapp/repositories/TypeRepository.java` ✓
