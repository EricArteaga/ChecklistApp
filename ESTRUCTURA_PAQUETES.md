# Estructura de Paquetes - ChecklistApp

## Feature-Based Architecture (2026-02-09)

```
src/main/java/com/example/checklistapp/
│
├── common/                          # Recursos compartidos
│   └── exception/
│       ├── ResourceNotFoundException.java
│       ├── BusinessRuleException.java
│       └── GlobalExceptionHandler.java
│
├── task/                            # Feature: Task (Tareas)
│   ├── controller/
│   │   └── TaskController.java      # REST: /api/tasks/*
│   ├── service/
│   │   └── TaskService.java         # Lógica de negocio
│   ├── repository/
│   │   └── TaskRepository.java      # JPA Repository
│   ├── dto/
│   │   ├── CreateTaskDTO.java
│   │   ├── UpdateTaskDTO.java
│   │   ├── TaskResponseDTO.java
│   │   └── TaskSummaryDTO.java
│   ├── mapper/
│   │   └── TaskMapper.java          # MapStruct (Entity ↔ DTO)
│   └── model/
│       └── Task.java                # JPA Entity
│
├── user/                            # Feature: User (Usuarios)
│   ├── repository/
│   │   └── UserRepository.java
│   ├── dto/
│   │   └── UserSummaryDTO.java
│   ├── mapper/
│   │   └── UserMapper.java
│   └── model/
│       └── User.java
│
├── type/                            # Feature: Type (Tipos de tareas)
│   ├── repository/
│   │   └── TypeRepository.java
│   ├── dto/
│   │   └── TypeSummaryDTO.java
│   ├── mapper/
│   │   └── TypeMapper.java
│   └── model/
│       └── Type.java
│
└── ChecklistAppApplication.java     # Spring Boot main class
```

## Endpoints TaskController

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/tasks` | Crear tarea |
| GET | `/api/tasks/{id}` | Obtener tarea por ID |
| GET | `/api/tasks` | Listar todas las tareas |
| GET | `/api/tasks/usuario/{idUsuario}` | Listar tareas de usuario |
| GET | `/api/tasks/usuario/{idUsuario}/pendientes` | Listar tareas pendientes |
| GET | `/api/tasks/usuario/{idUsuario}/completadas` | Listar tareas completadas |
| PATCH | `/api/tasks/{id}` | Actualizar tarea |
| DELETE | `/api/tasks/{id}` | Eliminar tarea |

## Reglas de Negocio

1. **Validación de referencias**: Usuario y Tipo deben existir
2. **Fecha de realización**: No puede ser anterior a fecha de programación
3. **Fecha automática**: Si marca completada sin fecha → LocalDate.now()
4. **idTipo opcional**: Una tarea puede no tener tipo

## Manejo de Errores

| Excepción | HTTP Status | Ejemplo |
|-----------|-------------|---------|
| ResourceNotFoundException | 404 | Tarea no encontrada con id: 123 |
| BusinessRuleException | 400 | Fecha de realización anterior a programación |
| MethodArgumentNotValidException | 400 | @NotBlank violations en DTOs |
| Exception | 500 | Errores internos del servidor
