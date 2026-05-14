# Linktic Technical Test — Juan Sebastian Becerra Bautista

Sistema de gestión de productos e inventario construido con **Java 21**, **Spring Boot** y **Gradle**.
Implementa un flujo de compra diferida usando **colas RabbitMQ** con lógica de reintentos automáticos y un frontend en **Quasar/Vue 3**.
Demuestra estándar JSON API, pruebas de integración con Testcontainers y orquestación con Docker Compose.

---

## Tabla de contenidos

1. [Requisitos](#requisitos)
2. [Instalación y ejecución](#instalación-y-ejecución)
3. [Arquitectura](#arquitectura)
4. [Diagrama de interacción entre servicios](#diagrama-de-interacción-entre-servicios)
5. [Documentación de la API](#documentación-de-la-api)
6. [Flujo de compra](#flujo-de-compra)
7. [Monitoreo de RabbitMQ](#monitoreo-de-rabbitmq)
8. [Decisiones técnicas](#decisiones-técnicas)
9. [Pruebas](#pruebas)
10. [Git Flow](#git-flow)
11. [Uso de herramientas de IA](#uso-de-herramientas-de-ia)

---

## Requisitos

- Docker Desktop / Colima
- Docker Compose v2
- Java 21 (solo para desarrollo local sin Docker)
- Node.js 22+ (solo para desarrollo local del frontend)

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/JSebastianBec/linktic_thecnical_test_jsbb.git
cd linktic_thecnical_test_jsbb
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
```

Editar `.env` con los valores deseados (los valores por defecto funcionan para Docker local):

```env
POSTGRES_DB=linktic_db
POSTGRES_USER=app_user
POSTGRES_PASSWORD=changeme
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
API_KEY=your-secret-api-key
VITE_API_KEY=your-secret-api-key
```

> `API_KEY` y `VITE_API_KEY` deben tener el mismo valor para que el frontend pueda autenticarse con el backend.

### 3. Compilar los servicios

```bash
# Compilar los servicios Java
cd product-service && ./gradlew bootJar --no-daemon && cd ..
cd inventory-service && ./gradlew bootJar --no-daemon && cd ..

# Instalar dependencias del frontend
cd frontend && npm install && cd ..
```

### 4. Vista previa del frontend (sin Docker)

Para revisar la interfaz sin levantar todo el ambiente:

```bash
cd frontend
npm install      # solo la primera vez
npx quasar dev
```

El frontend queda disponible en **http://localhost:9000**.

> En este modo el frontend arranca solo, sin conexión a los backends. Las llamadas a la API fallarán, pero sirve para revisar el diseño, la navegación y el layout.

---

### 5. Levantar el ambiente completo

```bash
# Primera vez — construye las imágenes e inicia todos los contenedores
docker-compose up --build

# Ejecuciones siguientes — inicia sin reconstruir (más rápido)
docker-compose up
```

> Los servicios arrancan en orden de dependencia: primero PostgreSQL y RabbitMQ, luego los backends, luego el frontend.

### 6. Detener

```bash
# Detiene los contenedores pero conserva los volúmenes y datos
docker-compose stop

# Detiene y elimina los contenedores (los datos se conservan en volúmenes)
docker-compose down
```

### 7. Reinicio limpio (borrar todos los datos)

Usar cuando se quiere un estado completamente fresco — base de datos vacía, colas vacías:

```bash
# Detiene los contenedores y elimina los volúmenes (se pierden todos los datos)
docker-compose down -v

# Reconstruye las imágenes e inicia desde cero
docker-compose up --build
```

> Usar `down -v` al cambiar de rama o después de cambios en el esquema para evitar conflictos de Hibernate con tablas existentes.

### URLs del sistema

| Servicio          | URL                                   |
|-------------------|---------------------------------------|
| Frontend          | http://localhost:9000                 |
| Product Service   | http://localhost:8080                 |
| Inventory Service | http://localhost:8081                 |
| Swagger Productos | http://localhost:8080/swagger-ui.html |
| Swagger Inventario| http://localhost:8081/swagger-ui.html |
| RabbitMQ UI       | http://localhost:15672                |

### Autenticación

Todos los endpoints de la API requieren el header:

```
X-API-KEY: <tu-api-key>
```

---

## Arquitectura

El sistema sigue una **arquitectura de microservicios** con dos servicios backend, un frontend y dos servicios de infraestructura:

```
┌─────────────────────────────────────────────────────────────┐
│                       Navegador                             │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Frontend (Quasar + Vue 3) :9000                │
│                    Nginx Reverse Proxy                      │
└──────────────┬──────────────────────────┬───────────────────┘
               │ /api/products/*          │ /api/inventory/*
               ▼                          ▼
┌──────────────────────┐    ┌──────────────────────────────────┐
│   product-service    │    │       inventory-service          │
│      :8080           │◄───│           :8081                  │
│                      │HTTP│  (consulta product-service       │
│  - Crear producto    │    │   vía WebClient)                 │
│  - Obtener por ID    │    │                                  │
│  - Listar todos      │    │  - Consultar stock               │
└──────────┬───────────┘    │  - Actualizar stock              │
           │                │  - Solicitar compra (async)      │
           │ publica        │  - Consultar estado de compra    │
           │ product.created└──────────────┬───────────────────┘
           ▼                               │ publica / consume
┌─────────────────────────────────────────▼───────────────────┐
│                       RabbitMQ :5672                        │
│                                                             │
│  products.exchange                                          │
│    └── inventory.product-created  ──► ProductCreatedListener│
│                                                             │
│  purchase.exchange                                          │
│    ├── purchase.requested  ──► PurchaseRequestedListener    │
│    └── purchase.wait (TTL=10s → regresa a purchase.requested│
└─────────────────────────────────────────────────────────────┘
           │                               │
           ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   PostgreSQL :5432                          │
│           (base de datos compartida: linktic_db)            │
│   tablas: products | inventory | purchase_requests          │
└─────────────────────────────────────────────────────────────┘
```

---

## Diagrama de interacción entre servicios

### Creación de un producto (síncrono + evento asíncrono)

```
Cliente               product-service           RabbitMQ         inventory-service
  │                        │                       │                    │
  │── POST /products ─────►│                       │                    │
  │                        │── guarda en BD ───────│                    │
  │                        │── publica ────────────► product.created    │
  │◄─── 201 Created ───────│                       │                    │
  │                        │                       │── entrega ────────►│
  │                        │                       │                    │── crea Inventory(stock=0)
```

### Solicitud de compra (asíncrono con reintentos)

```
Cliente          inventory-service        RabbitMQ             inventory-service
  │                    │                    │                  (listener)
  │── POST /purchases ─►│                   │                     │
  │                    │── guarda PENDING ──│                     │
  │                    │── publica ──────────► purchase.requested │
  │◄── 202 ACCEPTED ───│                    │                     │
  │                    │                    │── entrega ─────────►│
  │                    │                    │                     │── verifica stock
  │                    │                    │                     │
  │  (consulta estado) │                    │   [sin stock]       │── publica a purchase.wait
  │── GET /purchases/id►│                   │◄── TTL 10s ─────────│
  │◄── { PENDING } ────│                    │── entrega ─────────►│── verifica stock (intento 2)
  │                    │                    │                     │
  │                    │                    │   [stock disponible]│── descuenta stock
  │                    │                    │                     │── guarda COMPLETED
  │─ GET /purchases/id►│                   │                     │
  │◄── { COMPLETED } ──│                    │                     │
```

---

## Documentación de la API

### Product Service — `http://localhost:8080`

| Método | Endpoint                  | Descripción            | Auth       |
|--------|---------------------------|------------------------|------------|
| POST   | `/api/v1/products`        | Crear un producto      | Requerida  |
| GET    | `/api/v1/products`        | Listar todos           | Requerida  |
| GET    | `/api/v1/products/{id}`   | Obtener por ID         | Requerida  |
| GET    | `/actuator/health`        | Estado del servicio    | No requerida |

**Crear producto — request:**
```json
{
  "name": "Laptop",
  "price": 1500.00,
  "description": "Laptop gaming"
}
```

**Crear producto — response (JSON API):**
```json
{
  "data": {
    "id": "1",
    "type": "products",
    "attributes": {
      "id": 1,
      "name": "Laptop",
      "price": 1500.00,
      "description": "Laptop gaming"
    }
  }
}
```

---

### Inventory Service — `http://localhost:8081`

| Método | Endpoint                              | Descripción                    | Auth       |
|--------|---------------------------------------|--------------------------------|------------|
| GET    | `/api/v1/inventory`                   | Listar todo el inventario      | Requerida  |
| GET    | `/api/v1/inventory/{productId}`       | Consultar stock por producto   | Requerida  |
| PUT    | `/api/v1/inventory/{productId}/stock` | Actualizar stock               | Requerida  |
| POST   | `/api/v1/inventory/purchases`         | Solicitar compra (asíncrono)   | Requerida  |
| GET    | `/api/v1/inventory/purchases/{id}`    | Consultar estado de la compra  | Requerida  |
| GET    | `/actuator/health`                    | Estado del servicio            | No requerida |

**Solicitar compra — request:**
```json
{
  "productId": 1,
  "quantity": 5
}
```

**Solicitar compra — response (202 Accepted):**
```json
{
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "type": "purchases",
    "attributes": {
      "purchaseId": "550e8400-e29b-41d4-a716-446655440000",
      "productId": 1,
      "quantity": 5,
      "status": "PENDING",
      "attemptCount": 0,
      "failureReason": null,
      "createdAt": "2026-05-14T00:00:00Z",
      "resolvedAt": null
    }
  }
}
```

**Formato de error (JSON API):**
```json
{
  "errors": [
    {
      "status": "404",
      "title": "Product Not Found",
      "detail": "Product not found with id: 99"
    }
  ],
  "timestamp": "2026-05-14T00:00:00Z"
}
```

---

## Flujo de compra

El endpoint de compra fue implementado en **`inventory-service`** por las siguientes razones:

1. **Dueño del dominio** — el inventario es el contexto delimitado que posee el estado del stock. Una compra es una operación de escritura sobre datos de inventario, por lo tanto le corresponde a ese servicio.
2. **Bajo acoplamiento** — `product-service` solo gestiona el catálogo de productos. Agregar lógica de compras allí crearía una dependencia innecesaria con inventario.
3. **Responsabilidad única** — `inventory-service` es responsable de la consistencia del stock. Mantener la lógica de compra allí significa que un solo servicio controla el ciclo completo de una operación de stock.

### Flujo de compra diferida

La compra usa un **enfoque asíncrono basado en colas** en lugar de una respuesta HTTP sincrónica:

```
POST /purchases  →  202 Accepted + purchaseId
                        │
                   publicado a la cola purchase.requested
                        │
              PurchaseRequestedListener verifica stock
                        │
              ┌─── ¿hay stock? ────────┐
              │ SÍ                    │ NO
              ▼                       ▼
         descuenta stock        ¿intentos < 3?
         COMPLETED              │           │
                               SÍ          NO
                                ▼           ▼
                         publica a       FAILED
                         purchase.wait   "Sin stock tras
                         (TTL=10s)        3 intentos"
                                ▼
                         reintenta en 10s
```

**¿Por qué asíncrono?** Cuando se solicita una compra y el stock está temporalmente en cero (por ejemplo, se espera una reposición), el sistema espera hasta 30 segundos (3 intentos × 10s) antes de fallar. Esto evita que el cliente tenga que manejar manualmente los reintentos.

El cliente usa `GET /api/v1/inventory/purchases/{id}` para consultar el resultado. El frontend hace polling cada 3 segundos y se detiene automáticamente cuando el estado resuelve a `COMPLETED` o `FAILED`.

---

## Monitoreo de RabbitMQ

### Interfaz de administración

Abrir **http://localhost:15672** en el navegador después de ejecutar `docker-compose up`.

| Campo      | Valor   |
|------------|---------|
| Usuario    | `guest` |
| Contraseña | `guest` |

#### Secciones principales

**Pestaña Overview**
Tasas globales de mensajes — publicados, entregados, confirmados y no confirmados por segundo. Útil para verificar que los mensajes fluyen al crear un producto o al solicitar una compra.

**Pestaña Queues**
Muestra cada cola con su estado actual:

| Cola | Propósito |
|---|---|
| `inventory.product-created` | Recibe eventos de creación de producto — inicializa el inventario |
| `purchase.requested` | Punto de entrada para nuevas solicitudes de compra |
| `purchase.wait` | Retiene compras en espera durante 10s (TTL) antes de reenviarlas a `purchase.requested` |
| `purchase.dlq` | Cola de mensajes fallidos — los mensajes llegan aquí si el procesamiento falla repetidamente |

Al hacer clic en el nombre de una cola se puede ver:
- **Messages ready** — en espera de ser consumidos
- **Messages unacknowledged** — siendo procesados ahora mismo
- Botón **Get messages** — inspecciona el payload JSON de cualquier mensaje en la cola

**Pestaña Exchanges**
Muestra `products.exchange` y `purchase.exchange`. Al hacer clic en cada uno se ven los bindings — qué colas corresponden a cada routing key.

---

### Logs de los contenedores

Los logs más útiles para rastrear el flujo de mensajes están en `inventory-service`, ya que es quien consume y publica:

```bash
# Seguir logs de inventory-service (los listeners registran cada paso)
docker logs inventory-service -f

# Seguir logs de product-service (publica product.created en cada POST /products)
docker logs product-service -f

# Logs del broker RabbitMQ
docker logs rabbitmq -f

# Mostrar las últimas 50 líneas y continuar
docker logs inventory-service --tail 50 -f
```

#### Secuencia esperada de logs — crear producto + compra

```
# product-service — tras POST /products
INFO  ProductService : Product created: id=1, name=Laptop

# inventory-service — listener recibe product.created
INFO  ProductCreatedListener : Product created event received: productId=1, name=Laptop
INFO  ProductCreatedListener : Inventory initialized for productId=1 with stock=0

# inventory-service — tras POST /inventory/purchases (con stock disponible)
INFO  InventoryService          : Purchase request enqueued: id=<uuid>, productId=1, qty=3
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=3, attempt=1
INFO  PurchaseRequestedListener : Purchase COMPLETED: id=<uuid>, remaining stock=7

# inventory-service — flujo de reintentos (sin stock disponible)
INFO  InventoryService          : Purchase request enqueued: id=<uuid>, productId=1, qty=99
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=1
WARN  PurchaseRequestedListener : Insufficient stock — retry scheduled (attempt 1/3)
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=2
WARN  PurchaseRequestedListener : Insufficient stock — retry scheduled (attempt 2/3)
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=3
WARN  PurchaseRequestedListener : Purchase FAILED: id=<uuid> — insufficient stock after 3 attempts
```

---

## Decisiones técnicas

### Base de datos — PostgreSQL

Se eligió PostgreSQL sobre SQLite o NoSQL por las siguientes razones:

- El flujo de compra requiere **transacciones ACID** — cuando se descuenta el stock y se crea el registro de compra, ambas operaciones deben completarse o revertirse juntas.
- Ambos servicios comparten la misma instancia de PostgreSQL con tablas lógicamente separadas, lo que simplifica la configuración de Docker Compose sin sacrificar consistencia de datos.
- Spring Data JPA soporta PostgreSQL de forma nativa sin configuración adicional.

### Herramienta de build — Gradle (Kotlin DSL)

Se eligió Gradle con `build.gradle.kts` sobre Maven porque:

- Kotlin DSL provee seguridad de tipos y autocompletado en el IDE para los scripts de build.
- El build incremental y el caché de dependencias de Gradle son significativamente más rápidos que Maven en builds repetidos.
- La configuración de JaCoCo es más concisa y flexible en Gradle.

### Cliente HTTP — WebClient en lugar de RestTemplate

`inventory-service` usa `WebClient` (Spring WebFlux) en lugar de `RestTemplate` porque:

- `RestTemplate` está en modo mantenimiento desde Spring 5 y fue deprecado en Spring Framework 6.
- `WebClient` soporta `.timeout()` y `.retry()` directamente en la cadena reactiva sin necesitar Spring Retry + AOP.

### DTOs — Records de Java 21

Los DTOs de request y response usan `record` en lugar de clases con Lombok porque:

- Los records son inmutables por diseño, lo que previene mutaciones accidentales de los datos de entrada.
- Sin boilerplate: `equals`, `hashCode`, `toString` y los métodos de acceso se generan automáticamente.
- Las anotaciones de validación (`@NotBlank`, `@Positive`) funcionan en los componentes del record.

### Mensajería RabbitMQ — Serialización JSON

Los mensajes se serializan como **JSON** usando `Jackson2JsonMessageConverter` en lugar de serialización Java porque:

- JSON es agnóstico al lenguaje — si un nuevo servicio en otro lenguaje necesita consumir los eventos, puede hacerlo.
- La serialización Java es frágil: cualquier refactor que cambie la estructura de una clase rompe la deserialización de mensajes existentes.
- Ambos servicios declaran el mismo `products.exchange` de forma independiente, por lo que RabbitMQ lo crea de forma idempotente sin importar el orden de arranque.

---

## Pruebas

### Cobertura

| Servicio          | Tipo                    | Herramientas                         | Objetivo |
|-------------------|-------------------------|--------------------------------------|----------|
| product-service   | Unitarias + Integración | JUnit 5, Mockito, Testcontainers     | ≥ 80%    |
| inventory-service | Unitarias               | JUnit 5, Mockito                     | ≥ 80%    |

### Ejecutar pruebas

```bash
# product-service
cd product-service
./gradlew test jacocoTestReport jacocoTestCoverageVerification

# inventory-service
cd inventory-service
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

Los reportes de cobertura se generan en `build/reports/jacoco/test/html/index.html`.

```bash
# Abrir reporte en el navegador (Mac)
open product-service/build/reports/jacoco/test/html/index.html
open inventory-service/build/reports/jacoco/test/html/index.html
```

### Qué se prueba

**product-service:**
- `ProductService` — crear, obtener por ID (encontrado / no encontrado), listar todos
- `ProductController` — formato JSON API, códigos de estado 201/200/404, 401 sin API key
- `GlobalExceptionHandler` — formato de error 404 y 400
- `ProductIntegrationTest` — flujo HTTP completo con PostgreSQL real vía Testcontainers

**inventory-service:**
- `InventoryService` — getStock, updateStock, requestPurchase, getPurchaseStatus (todas las ramas)
- `InventoryController` — todos los endpoints, formato JSON API, códigos de error
- `GlobalExceptionHandler` — formato de error 404, 503, 500
- `PurchaseRequestedListener` — stock disponible → COMPLETED, sin stock + reintento, sin stock + máximo intentos → FAILED, solicitud no encontrada → omitir
- `ProductCreatedListener` — crea inventario, omite si ya existe

---

## Git Flow

Este proyecto sigue el modelo de ramas **Git Flow**:

```
main          ← solo recibe merges de release/*
develop       ← rama de integración
feature/*     ← ramas de funcionalidades individuales
release/*     ← preparación de versiones
bugfix/*      ← solucion de issues
hotfix/*      ← solucion de issues en producción
```

### Convención de commits

Los commits siguen [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(product): add create product endpoint
fix(inventory): handle null stock on purchase
test(inventory): add retry queue listener tests
chore(infra): add docker-compose healthchecks
```

---

## Uso de herramientas de IA

### Herramientas utilizadas

| Herramienta  | Propósito                                                                    |
|--------------|------------------------------------------------------------------------------|
| Claude Code  | Diseño de arquitectura, revisión de código, generación de tests, documentación |

### Cómo se usó la IA

**Diseño de arquitectura y decisiones técnicas**
Claude Code se utilizó para evaluar las ventajas y desventajas entre el enfoque de compra síncrono y asíncrono. El patrón de cola diferida con reintento basado en TTL fue propuesto y refinado a través de conversación, comparándolo con alternativas sincrónicas más simples.

**Scaffolding inicial**
Los proyectos Spring Boot se generaron vía Spring Initializr (start.spring.io). Los archivos `build.gradle.kts` se configuraron con las versiones correctas de dependencias y reglas de exclusión de JaCoCo.

**Revisión de código y calidad**
Después de cada fase de implementación, Claude Code revisó el código buscando:
- Anotaciones `@Transactional` faltantes
- Consistencia de JSON API en todos los endpoints
- Firmas de métodos ambiguas en verificaciones de Mockito
- APIs deprecadas de Spring Boot 3.5 (`@MockBean` → `@MockitoBean`)

**Generación de pruebas**
Las pruebas unitarias de servicios, pruebas de controladores con `@WebMvcTest` y la estructura de pruebas de integración con Testcontainers fueron generadas y luego verificadas manualmente ejecutando `./gradlew test`.

### Verificación de calidad

Todo el código generado por IA fue verificado mediante:
1. Ejecución de la suite completa de pruebas (`./gradlew test`)
2. Revisión de los reportes de cobertura JaCoCo para confirmar que se alcanza el mínimo del 80%
3. Inspección manual de las aserciones de los tests para confirmar que corresponden a la lógica de negocio real
4. Ejecución de `docker-compose up` para validar el comportamiento de extremo a extremo

La IA se utilizó para acelerar el desarrollo, no para reemplazar la comprensión. Cada archivo generado fue leído, entendido y ajustado antes de ser confirmado en el repositorio.
