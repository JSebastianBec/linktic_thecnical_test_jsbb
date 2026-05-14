# Linktic Technical Test — JSBB

Microservices system for product and inventory management built with **Java 21**, **Spring Boot**, and **Gradle**.
Features a deferred purchase flow using **RabbitMQ queues** with automatic retry logic, and a **Quasar/Vue 3** frontend.
Demonstrates Git Flow, JSON API standard, Testcontainers integration testing, and Docker Compose orchestration.

---

## Table of Contents

1. [Requirements](#requirements)
2. [Installation & Execution](#installation--execution)
3. [Architecture](#architecture)
4. [Service Interaction Diagram](#service-interaction-diagram)
5. [API Documentation](#api-documentation)
6. [Purchase Flow](#purchase-flow)
7. [Monitoring RabbitMQ](#monitoring-rabbitmq)
8. [Technical Decisions](#technical-decisions)
9. [Testing](#testing)
10. [Git Flow](#git-flow)
11. [AI Tools Usage](#ai-tools-usage)

---

## Requirements

- Docker Desktop / Colima
- Docker Compose v2
- Java 21 (only for local development without Docker)
- Node.js 22+ (only for local frontend development)

---

## Installation & Execution

### 1. Clone the repository

```bash
git clone https://github.com/JSebastianBec/linktic_thecnical_test_jsbb.git
cd linktic_thecnical_test_jsbb
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` with your values (defaults work for local Docker):

```env
POSTGRES_DB=linktic_db
POSTGRES_USER=app_user
POSTGRES_PASSWORD=changeme
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
API_KEY=your-secret-api-key
```

### 3. Build the services

```bash
# Build Java services before Docker
cd product-service && ./gradlew bootJar --no-daemon && cd ..
cd inventory-service && ./gradlew bootJar --no-daemon && cd ..

# Install frontend dependencies
cd frontend && npm install && cd ..
```

### 4. Start

```bash
# First run — builds images and starts all containers
docker-compose up --build

# Subsequent runs — starts without rebuilding (faster)
docker-compose up
```

> All services start in dependency order: PostgreSQL and RabbitMQ first, then the backends, then the frontend.

### 5. Stop

```bash
# Stop all containers but keep volumes and data
docker-compose stop

# Stop and remove containers (data is preserved in volumes)
docker-compose down
```

### 6. Clean restart (wipe all data)

Use this when you want a completely fresh state — empty database, empty queues:

```bash
# Stop containers and delete volumes (all data is lost)
docker-compose down -v

# Rebuild images and start fresh
docker-compose up --build
```

> Use `down -v` when switching branches or after schema changes to avoid Hibernate conflicts with existing tables.

### Service URLs

| Service           | URL                                        |
|-------------------|--------------------------------------------|
| Frontend          | http://localhost:9000                      |
| Product Service   | http://localhost:8080                      |
| Inventory Service | http://localhost:8081                      |
| Swagger Products  | http://localhost:8080/swagger-ui.html      |
| Swagger Inventory | http://localhost:8081/swagger-ui.html      |
| RabbitMQ UI       | http://localhost:15672                     |

### Authentication

All API endpoints require the header:

```
X-API-KEY: <your-api-key>
```

---

## Architecture

The system follows a **microservices architecture** with two backend services, one frontend, and two infrastructure services:

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
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
│                      │HTTP│  (calls product-service for      │
│  - Create product    │    │   product info via WebClient)    │
│  - Get by ID         │    │                                  │
│  - List all          │    │  - Get stock                     │
└──────────┬───────────┘    │  - Update stock                  │
           │                │  - Request purchase (async)      │
           │ publishes      │  - Get purchase status           │
           │ product.created└──────────────┬───────────────────┘
           ▼                               │ publishes / consumes
┌─────────────────────────────────────────▼───────────────────┐
│                       RabbitMQ :5672                        │
│                                                             │
│  products.exchange                                          │
│    └── inventory.product-created  ──► ProductCreatedListener│
│                                                             │
│  purchase.exchange                                          │
│    ├── purchase.requested  ──► PurchaseRequestedListener    │
│    └── purchase.wait (TTL=10s → back to purchase.requested) │
└─────────────────────────────────────────────────────────────┘
           │                               │
           ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   PostgreSQL :5432                          │
│           (shared database: linktic_db)                     │
│   tables: products | inventory | purchase_requests          │
└─────────────────────────────────────────────────────────────┘
```

---

## Service Interaction Diagram

### Creating a product (synchronous + async event)

```
Client                product-service           RabbitMQ         inventory-service
  │                        │                       │                    │
  │── POST /products ──────►│                       │                    │
  │                        │── save to DB ─────────│                    │
  │                        │── publish ────────────► product.created     │
  │◄─── 201 Created ───────│                       │                    │
  │                        │                       │── deliver ─────────►│
  │                        │                       │                    │── create Inventory(stock=0)
```

### Requesting a purchase (asynchronous with retry)

```
Client           inventory-service        RabbitMQ             inventory-service
  │                    │                     │                  (listener)
  │── POST /purchases ─►│                    │                     │
  │                    │── save PENDING ─────│                     │
  │                    │── publish ──────────► purchase.requested   │
  │◄── 202 ACCEPTED ───│                    │                     │
  │                    │                    │── deliver ──────────►│
  │                    │                    │                     │── check stock
  │                    │                    │                     │
  │  (polls status)    │                    │   [no stock]        │── publish to purchase.wait
  │── GET /purchases/id►│                   │◄── TTL 10s ─────────│
  │◄── { PENDING } ────│                    │── deliver ──────────►│── check stock (attempt 2)
  │                    │                    │                     │
  │                    │                    │   [stock available] │── deduct stock
  │                    │                    │                     │── save COMPLETED
  │── GET /purchases/id►│                   │                     │
  │◄── { COMPLETED } ──│                    │                     │
```

---

## API Documentation

### Product Service — `http://localhost:8080`

| Method | Endpoint                  | Description         | Auth     |
|--------|---------------------------|---------------------|----------|
| POST   | `/api/v1/products`        | Create a product    | Required |
| GET    | `/api/v1/products`        | List all products   | Required |
| GET    | `/api/v1/products/{id}`   | Get product by ID   | Required |
| GET    | `/actuator/health`        | Health check        | None     |

**Create product — request:**
```json
{
  "name": "Laptop",
  "price": 1500.00,
  "description": "Gaming laptop"
}
```

**Create product — response (JSON API):**
```json
{
  "data": {
    "id": "1",
    "type": "products",
    "attributes": {
      "id": 1,
      "name": "Laptop",
      "price": 1500.00,
      "description": "Gaming laptop"
    }
  }
}
```

---

### Inventory Service — `http://localhost:8081`

| Method | Endpoint                              | Description              | Auth     |
|--------|---------------------------------------|--------------------------|----------|
| GET    | `/api/v1/inventory/{productId}`       | Get stock by product     | Required |
| PUT    | `/api/v1/inventory/{productId}/stock` | Update stock             | Required |
| POST   | `/api/v1/inventory/purchases`         | Request purchase (async) | Required |
| GET    | `/api/v1/inventory/purchases/{id}`    | Get purchase status      | Required |
| GET    | `/actuator/health`                    | Health check             | None     |

**Request purchase — request:**
```json
{
  "productId": 1,
  "quantity": 5
}
```

**Request purchase — response (202 Accepted):**
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

**Error response format (JSON API):**
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

## Purchase Flow

The purchase endpoint was implemented in **`inventory-service`** for the following reasons:

1. **Domain ownership** — inventory is the bounded context that owns stock state. A purchase is a write operation on inventory data, so it belongs there.
2. **Low coupling** — `product-service` only manages the product catalog. Adding purchase logic there would create an undesired dependency on inventory.
3. **Single responsibility** — `inventory-service` is responsible for stock consistency. Keeping purchase logic there means one service controls the full lifecycle of a stock operation.

### Deferred purchase flow

The purchase uses an **asynchronous queue-based approach** instead of a synchronous HTTP response:

```
POST /purchases  →  202 Accepted + purchaseId
                        │
                   published to purchase.requested queue
                        │
              PurchaseRequestedListener checks stock
                        │
              ┌─── stock available? ───┐
              │ YES                   │ NO
              ▼                       ▼
         deduct stock          attempt < 3?
         COMPLETED              │         │
                               YES       NO
                                ▼         ▼
                         publish to    FAILED
                         purchase.wait  "Insufficient stock
                         (TTL=10s)       after 3 attempts"
                                ▼
                         retry after 10s
```

**Why asynchronous?** When a purchase is requested and stock is temporarily unavailable (e.g., a stock update is expected), the system waits up to 30 seconds (3 attempts × 10s) before failing. This avoids forcing the client to poll the product's availability manually.

The client uses `GET /api/v1/inventory/purchases/{id}` to poll the result. The frontend polls every 3 seconds and stops automatically when the status resolves to `COMPLETED` or `FAILED`.

---

## Monitoring RabbitMQ

### Management UI

Open **http://localhost:15672** in the browser after running `docker-compose up`.

| Field    | Value   |
|----------|---------|
| Username | `guest` |
| Password | `guest` |

#### Key sections

**Overview tab**
Global message rates — published, delivered, acknowledged and unacknowledged per second. Useful to confirm messages are flowing when a product is created or a purchase is requested.

**Queues tab**
Shows each queue with its current state:

| Queue | Purpose |
|---|---|
| `inventory.product-created` | Receives `product.created` events — triggers inventory initialization |
| `purchase.requested` | Entry point for new purchase requests |
| `purchase.wait` | Holds retried purchases for 10 s (TTL) before re-routing back to `purchase.requested` |
| `purchase.dlq` | Dead letter queue — messages land here if processing fails repeatedly |

Click any queue name to see:
- **Messages ready** — waiting to be consumed
- **Messages unacknowledged** — being processed right now
- **Get messages** button — inspect the raw JSON payload of any message in the queue

**Exchanges tab**
Shows `products.exchange` and `purchase.exchange`. Click either to see bindings — which queues each routing key maps to.

---

### Container logs

The most useful logs for tracing the message flow are in `inventory-service`, since it both consumes and publishes:

```bash
# Follow inventory-service logs (listeners log every step)
docker logs inventory-service -f

# Follow product-service logs (publishes product.created on every POST /products)
docker logs product-service -f

# RabbitMQ broker logs
docker logs rabbitmq -f

# Show only the last 50 lines then follow
docker logs inventory-service --tail 50 -f
```

#### Expected log sequence — create product + purchase

```
# product-service — after POST /products
INFO  ProductService : Product created: id=1, name=Laptop

# inventory-service — listener receives product.created
INFO  ProductCreatedListener : Product created event received: productId=1, name=Laptop
INFO  ProductCreatedListener : Inventory initialized for productId=1 with stock=0

# inventory-service — after POST /inventory/purchases (stock available)
INFO  InventoryService          : Purchase request enqueued: id=<uuid>, productId=1, qty=3
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=3, attempt=1
INFO  PurchaseRequestedListener : Purchase COMPLETED: id=<uuid>, remaining stock=7

# inventory-service — retry flow (no stock available)
INFO  InventoryService          : Purchase request enqueued: id=<uuid>, productId=1, qty=99
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=1
WARN  PurchaseRequestedListener : Insufficient stock — retry scheduled (attempt 1/3)
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=2
WARN  PurchaseRequestedListener : Insufficient stock — retry scheduled (attempt 2/3)
INFO  PurchaseRequestedListener : Processing purchase: id=<uuid>, productId=1, qty=99, attempt=3
WARN  PurchaseRequestedListener : Purchase FAILED: id=<uuid> — insufficient stock after 3 attempts
```

---

## Technical Decisions

### Database — PostgreSQL

PostgreSQL was chosen over SQLite or NoSQL for these reasons:

- The purchase flow requires **ACID transactions** — when stock is deducted and a purchase record is created, both must succeed or both must roll back.
- Both services share the same PostgreSQL instance with logically separate tables, which simplifies the Docker Compose setup without sacrificing data consistency.
- Spring Data JPA supports PostgreSQL natively with no extra configuration.

### Build tool — Gradle (Kotlin DSL)

Gradle with `build.gradle.kts` was chosen over Maven because:

- Kotlin DSL provides type-safety and IDE autocompletion for build scripts.
- Gradle's incremental build and dependency caching is significantly faster than Maven on repeated builds.
- JaCoCo coverage configuration is more concise and flexible in Gradle.

### HTTP Client — WebClient instead of RestTemplate

`inventory-service` uses `WebClient` (Spring WebFlux) instead of `RestTemplate` because:

- `RestTemplate` is in maintenance mode since Spring 5 and deprecated in Spring 6.
- `WebClient` supports `.timeout()` and `.retry()` directly in the reactive chain without needing Spring Retry + AOP.

### DTOs — Java 21 Records

Request and response DTOs use `record` instead of classes with Lombok because:

- Records are immutable by design, which prevents accidental mutation of request data.
- No boilerplate: `equals`, `hashCode`, `toString`, and accessor methods are generated automatically.
- Bean validation annotations (`@NotBlank`, `@Positive`) work on record components.

### RabbitMQ messaging — JSON serialization

Messages are serialized as **JSON** using `Jackson2JsonMessageConverter` instead of Java serialization because:

- JSON is language-agnostic — if a new service in a different language needs to consume the events, it can.
- Java serialization is fragile: any refactor that changes class structure breaks deserialization of existing messages.
- Both services declare the same `products.exchange` independently, so RabbitMQ creates it idempotently regardless of startup order.

---

## Testing

### Coverage

| Service           | Type                | Tools                        | Target |
|-------------------|---------------------|------------------------------|--------|
| product-service   | Unit + Integration  | JUnit 5, Mockito, Testcontainers | ≥ 80% |
| inventory-service | Unit                | JUnit 5, Mockito             | ≥ 80% |

### Run tests

```bash
# product-service
cd product-service
./gradlew test jacocoTestReport

# inventory-service
cd inventory-service
./gradlew test jacocoTestReport
```

Coverage reports are generated at `build/reports/jacoco/test/html/index.html`.

### What is tested

**product-service:**
- `ProductService` — create, getById (found / not found), getAll
- `ProductController` — JSON API format, 201/200/404 status codes, 401 when no API key
- `GlobalExceptionHandler` — 404 and 400 error format
- `ProductIntegrationTest` — full HTTP flow with PostgreSQL via Testcontainers

**inventory-service:**
- `InventoryService` — getStock, updateStock, requestPurchase, getPurchaseStatus (all branches)
- `InventoryController` — all endpoints, JSON API format, error codes
- `GlobalExceptionHandler` — 404, 503, 500 error format
- `PurchaseRequestedListener` — stock available → COMPLETED, no stock + retry, no stock + max attempts → FAILED, purchase not found → skip
- `ProductCreatedListener` — creates inventory, skips if already exists

---

## Git Flow

This project follows the **Git Flow** branching model:

```
main          ← production releases only
develop       ← integration branch
feature/*     ← individual feature branches
release/*     ← release preparation
```

### Commit convention

Commits follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(product): add create product endpoint
fix(inventory): handle null stock on purchase
test(inventory): add retry queue listener tests
chore(infra): add docker-compose healthchecks
```

---
