# ChecklistApp Monorepo

Proyecto monorepo para una aplicación de CHECKLIST con HEATMAP.

Descripción:
- Frontend web: React
- Móvil: React Native
- Backend: Java Spring Boot
- Base de datos: PostgreSQL
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
- Muchas partes están marcadas con `TODO` y placeholders donde debe implementarse la lógica de negocio.
