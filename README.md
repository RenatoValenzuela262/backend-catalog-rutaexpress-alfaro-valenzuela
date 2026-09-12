# ms-rutaexpress-catalog

Microservicio de **catálogo** de RutaExpress (plataforma de envíos de última
milla). Administra los **servicios de envío** ofrecidos (nombre, tarifa, código)
y la **capacidad de flota** disponible para cada uno.

No contiene lógica de envíos, notificaciones ni auditoría: es llamado de forma
síncrona por `ms-rutaexpress-shipments` para descontar/repone capacidad cuando
un envío pasa a `ACEPTADO` o se cancela antes de `EN_RUTA`.

---

## Stack y dependencias

- Java 21 + Spring Boot 4.1.x (Spring Web MVC + Spring Data JPA).
- Validación `jakarta.validation` en los DTOs.
- `springdoc-openapi` v3 para Swagger UI (`/swagger-ui.html`).
- Lombok + MapStruct (mapeo entity ↔ DTO).
- Actuator (`/actuator/health`) para healthcheck de Docker/EC2.
- BD: **H2** en memoria (perfil `local`/tests) y **PostgreSQL** en Docker
  (perfil `docker`).
- JUnit 5 + Mockito para los tests unitarios de la capa de servicio.
- Sin Spring Security en esta iteración: `/api/catalog/**` está abierto.

## Estructura de paquetes

```
com.rutaexpress.catalog
├── CatalogServiceApplication.java
├── config/          (OpenApiConfig, JpaAuditingConfig)
├── controller/      (CatalogController, CapacityController)
├── service/         (CatalogService, CapacityService, impl/)
├── repository/      (ShippingServiceRepository)
├── entities/        (ShippingServiceEntity)
├── dto/             (request/, response/)
├── mapper/          (ServiceMapper)
└── exception/       (ServiceNotFoundException, DuplicateServiceCodeException,
                      InsufficientCapacityException, ConcurrentCapacityUpdateException,
                      GlobalExceptionHandler)
```

---

## Decisiones técnicas (documentadas)

1. **PostgreSQL como BD productiva.** El `system_prompt.md` menciona
   "Postgres en producción/docker" y el código usa `GenerationType.SEQUENCE`,
   ambas cosas propias de PostgreSQL. El driver `ojdbc11` mencionado en el
   prompt era un error de copy-paste (era de Oracle): se usa el driver
   **`org.postgresql:postgresql`** y la imagen **`postgres:16-alpine`** en
   Docker.
2. **Excepción extra.** Se agregó `ConcurrentCapacityUpdateException`
   (HTTP 409) más allá de las cuatro del prompt, para cuando el reintento del
   bloqueo optimista se agota.
3. **`GET /api/catalog/services/{id}`** devuelve también servicios inactivos
   (soft-deleted): la fila sigue existiendo por referencias históricas. En
   cambio, **listado, PUT, DELETE y ajustes de capacidad** únicamente operan
   sobre servicios activos (inactivo ⇒ 404).
4. **`PUT` (UpdateServiceRequest):** `availableCapacity` se mueve junto con
   `totalCapacity` (misma diferencia). Si el resultado fuese negativo (bajaría
   de la capacidad ya comprometida) ⇒ HTTP 409.
5. **`increase`:** repone `amount` pero **sin exceder** `totalCapacity`
   (se recorta al tope).
6. **Ajustes atómicos de capacidad:** cada operación corre en una transacción
   propia (`TransactionTemplate`). Se usa `@Version` (lock optimista); si el
   flush falla por contienda se **reintenta una vez** con datos frescos y, si
   el conflicto persiste, se responde **HTTP 409**.
7. El `code` se **recorta (trim)** al crear y queda **inmutable** (no está en
   el `UpdateServiceRequest`). La unicidad se valida por `existsByCode` y la
   columna tiene constraint `UNIQUE`.

## Reglas de negocio implementadas

- `code` único e inmutable tras la creación.
- `availableCapacity` nunca es `< 0` ni `> totalCapacity`.
- Al crear: `availableCapacity = totalCapacity`.
- Servicio inactivo no recibe ajustes de capacidad ni figura en el listado por
  defecto (solo con `?active=false` explícito).

---

## Cómo levantar

### Local (H2, sin Docker)

Requisito: Java 21 y Maven (o usar el wrapper `./mvnw`).

```bash
./mvnw clean package          # compila y corre los tests
./mvnw spring-boot:run        # usa el perfil local por defecto (H2 en memoria)
# o bien:
java -jar target/catalog-0.0.1-SNAPSHOT.jar
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>
- Consola H2: <http://localhost:8080/h2-console> (`jdbc:h2:mem:rutacatalog`,
  usuario `sa`, sin password).

Los datos no persisten entre reinicios (H2 en memoria, `create-drop`).

### Docker (PostgreSQL)

```bash
docker compose up --build
```

Levanta la BD **PostgreSQL 16** (`postgres:16-alpine`) con base/usuario
`rutaexpress`, espera a que esté sana (`pg_isready` vía `service_healthy`) y
arranca el microservicio con el perfil `docker`.

La primera vez se crea el schema automáticamente (`DDL_AUTO=update`); podés
ajustarlo con la variable `DDL_AUTO`. El `docker-compose.yml` también expone
PostgreSQL en `localhost:5432` para conectarte con tu cliente favorito
(`rutaexpress`/`rutaexpress`, base `rutaexpress`).

Variables de entorno del perfil `docker` (con defaults):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/rutaexpress` |
| `DB_USER` | `rutaexpress` |
| `DB_PASSWORD` | `rutaexpress` |
| `DDL_AUTO` | `update` |

---

## Endpoints

Base: `/api/catalog/services`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/catalog/services?active=true&page=0&size=20` | Lista paginada (default activos) | `200` |
| GET | `/api/catalog/services/{id}` | Obtiene un servicio | `200` / `404` |
| POST | `/api/catalog/services` | Crea (code único, numéricos > 0) | `201` / `400` / `409` |
| PUT | `/api/catalog/services/{id}` | Actualiza (name/tariff/totalCapacity) | `200` / `400` / `404` / `409` |
| DELETE | `/api/catalog/services/{id}` | Soft delete (`active=false`) | `204` / `404` |
| PATCH | `/api/catalog/services/{id}/capacity/decrease` | Descuenta capacidad (envío ACEPTADO) | `204` / `400` / `404` / `409` |
| PATCH | `/api/catalog/services/{id}/capacity/increase` | Repone capacidad (envío cancelado) | `204` / `400` / `404` / `409` |

### Ejemplos con curl

```bash
# Crear un servicio
curl -X POST http://localhost:8080/api/catalog/services \
  -H 'Content-Type: application/json' \
  -d '{"code":"EXPRESS_2H","name":"Express 2 horas","description":"Entrega < 2h","tariff":2500.00,"totalCapacity":100}'

# Listar activos (paginado)
curl "http://localhost:8080/api/catalog/services?active=true&page=0&size=20"

# Descontar capacidad (lo llama ms-rutaexpress-shipments al ACEPTAR un envío)
curl -X PATCH http://localhost:8080/api/catalog/services/1/capacity/decrease \
  -H 'Content-Type: application/json' -d '{"amount":1}'

# Reponer capacidad (envío cancelado) — nunca supera totalCapacity
curl -X PATCH http://localhost:8080/api/catalog/services/1/capacity/increase \
  -H 'Content-Type: application/json' -d '{"amount":1}'

# Soft delete
curl -X DELETE http://localhost:8080/api/catalog/services/1
```

### Formato de error

```json
{
  "timestamp": "2026-09-12T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Capacidad insuficiente para el servicio EXPRESS_2H",
  "path": "/api/catalog/services/3/capacity/decrease"
}
```

---

## Tests

```bash
./mvnw test
```

- `CatalogServiceImplTest` — alta con code duplicado (409), available=total,
  update que aumenta/disminuye capacidad, soft delete, 404.
- `CapacityServiceImplTest` — decrease exitoso, capacidad insuficiente,
  increase con tope en totalCapacity, servicio inactivo/no encontrado, y
  reintento por bloqueo optimista (éxito al 2.º intento y 409 si persiste).

## Futuro (fuera de alcance)

Integración con Azure AD (JWT) detrás del API Gateway con roles
`ADMIN`/`OPERADOR`, `SecurityConfig`, conversión de claims y `@PreAuthorize`;
`spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`;
migraciones de schema con Flyway/Liquibase en lugar de `ddl-auto`.