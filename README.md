# ChecklistApp Monorepo

Proyecto monorepo para una aplicación de CHECKLIST con HEATMAP.

Descripción:
- Frontend web: React 18 + Tailwind CSS
- Móvil: React Native (pendiente)
- Backend: Java Spring Boot 3.1.3
- Base de datos: PostgreSQL (producción), H2 (tests)
- Contenedores: Docker

Estructura del monorepo:
- `apps/` - Aplicaciones (web y móvil)
- `services/api/` - Backend Spring Boot
- `packages/` - Paquetes compartidos (UI, DB, lib)
- `infra/` - Infraestructura (CI)

Cómo levantar todo con Docker (modo recomendado):

1. Copiar el archivo de ejemplo de variables de entorno y completar los valores:

```bash
cp .env.example .env
# editar .env y completar credenciales
```

2. Iniciar con Docker Compose:

```bash
docker compose up --build
```

Comandos principales:
- Levantar servicios: `docker compose up --build`
- Construir imágenes: `docker compose build`
- Parar y eliminar: `docker compose down`

Notas:
- Los archivos `.env` reales no están incluidos. Use `.env.example` como referencia.
- El backend usa el paquete Java `com.example.checklistapp`.

## Testing

### Ejecutar Tests

```bash
# Todos los tests (179/263 tests passing)
cd services/api
./gradlew test

# Solo Repository tests (60/60 passing ✅)
./gradlew test --tests "*RepositoryTest"

# Solo Service tests (65/65 passing ✅)
./gradlew test --tests "*ServiceTest"

# Solo Controller tests (12/90 passing ⚠️)
./gradlew test --tests "*ControllerTest"
```

### Configuración de Tests

- **Base de datos**: H2 en memoria (Flyway deshabilitado)
- **Velocidad**: ~17 segundos total (4s unit + 13s integration)
- **Cobertura**: 179/263 tests (68%)
  - Repository: 60/60 (100%) ✅
  - Service: 65/65 (100%) ✅
  - Mapper: 24/32 (75%) ✅
  - Controller: 12/90 (13%) ⚠️

### Tecnologías de Testing

- **Unit Tests**: JUnit 5 + Mockito + AssertJ
- **Integration Tests**: @DataJpaTest (H2) + @WebMvcTest
- **Patrón**: Given-When-Then

Ver documentación completa: `memory/testing.md`
