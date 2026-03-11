# MicroApp – Spring Boot Microservices

A two-service Spring Boot microservices system that demonstrates both **synchronous REST communication** and **asynchronous event-driven messaging** via RabbitMQ (CloudAMQP). The system manages products and orders, where services communicate in real time to validate stock and process order lifecycle events.

---

## Architecture Overview

MicroApp consists of two independently deployable Spring Boot services:

```
┌──────────────────────────────────────────────────────────────────────┐
│                          CLIENT / BROWSER                            │
└──────────┬────────────────────────────────────┬──────────────────────┘
           │ POST /orders                       │ GET /products/{id}
           ▼                                    ▼
┌─────────────────────┐              ┌─────────────────────┐
│    order-service    │              │   product-service   │
│    (port 8080)      │              │    (port 8081)      │
│                     │─────REST────▶│                     │
│  - Creates orders   │ GET product  │  - Manages products │
│  - Validates stock  │◀─────────────│  - Exposes product  │
│  - Publishes events │              │    data via REST    │
│  - H2: orderdb      │              │  - Consumes events  │
│                     │              │  - H2: productdb    │
└──────────┬──────────┘              └──────────▲──────────┘
           │                                     │
           │  Publish: OrderCreatedEvent         │ Consume: OrderCreatedEvent
           │  Exchange: orders.exchange          │ Queue: product.order.created.queue
           │  Routing Key: order.created         │
           ▼                                     │
┌──────────────────────────────────────────────────────────────────────┐
│                    RabbitMQ (CloudAMQP)                              │
│            Exchange: orders.exchange (topic)                         │
│            Queue:    product.order.created.queue                     │
└──────────────────────────────────────────────────────────────────────┘
```

### Service Responsibilities

| Service | Responsibilities |
|---|---|
| **order-service** | Accepts order creation requests. Calls product-service synchronously via REST to validate product existence and stock. Persists the order, then publishes an `OrderCreatedEvent` to RabbitMQ. |
| **product-service** | Manages product data (name, price, stock). Exposes REST endpoints for querying and creating products. Consumes `OrderCreatedEvent` messages from RabbitMQ and reduces stock accordingly. |

### Communication Flows

**Synchronous (REST):** When a client sends `POST /orders`, the order-service makes a blocking HTTP call to the product-service (`GET /products/{id}`) to verify the product exists and has sufficient stock before accepting the order.

**Asynchronous (RabbitMQ):** After an order is persisted, the order-service publishes an `OrderCreatedEvent` to the `orders.exchange` topic exchange. The product-service listens on its dedicated queue (`product.order.created.queue`) and deducts the ordered quantity from the product's stock upon receipt.

---

## Repository Structure

```
MicroApp/
├── architecture.md                   ← Original architecture plan and API contracts
├── README.md                         ← This file
│
├── order-service/                    ← Microservice 1 (port 8080)
│   ├── pom.xml                       ← Maven build descriptor
│   └── src/
│       ├── main/
│       │   ├── java/com/microapp/orderservice/
│       │   │   ├── OrderServiceApplication.java          ← Entry point
│       │   │   ├── config/
│       │   │   │   └── AppConfig.java                    ← RestTemplate bean
│       │   │   ├── controller/
│       │   │   │   ├── OrderController.java              ← REST endpoints
│       │   │   │   └── GlobalExceptionHandler.java       ← Error handling
│       │   │   ├── service/
│       │   │   │   └── OrderService.java                 ← Business logic
│       │   │   ├── client/
│       │   │   │   └── ProductClient.java                ← Sync REST client
│       │   │   ├── dto/
│       │   │   │   ├── CreateOrderRequest.java
│       │   │   │   ├── OrderResponse.java
│       │   │   │   ├── ProductDto.java
│       │   │   │   └── OrderCreatedEvent.java
│       │   │   ├── model/
│       │   │   │   └── Order.java                        ← JPA entity
│       │   │   ├── repository/
│       │   │   │   └── OrderRepository.java              ← Spring Data JPA
│       │   │   └── messaging/
│       │   │       ├── config/
│       │   │       │   └── RabbitMQConfig.java           ← Exchange declaration
│       │   │       └── producer/
│       │   │           └── OrderEventPublisher.java      ← Event publisher
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
└── product-service/                  ← Microservice 2 (port 8081)
    ├── pom.xml                       ← Maven build descriptor
    └── src/
        ├── main/
        │   ├── java/com/microapp/productservice/
        │   │   ├── ProductServiceApplication.java        ← Entry point
        │   │   ├── controller/
        │   │   │   ├── ProductController.java            ← REST endpoints
        │   │   │   └── GlobalExceptionHandler.java       ← Error handling
        │   │   ├── service/
        │   │   │   └── ProductService.java               ← Business logic
        │   │   ├── dto/
        │   │   │   ├── ProductResponse.java
        │   │   │   └── OrderCreatedEvent.java
        │   │   ├── model/
        │   │   │   └── Product.java                      ← JPA entity
        │   │   ├── repository/
        │   │   │   └── ProductRepository.java            ← Spring Data JPA
        │   │   └── messaging/
        │   │       ├── config/
        │   │       │   └── RabbitMQConfig.java           ← Exchange + queue + binding
        │   │       └── consumer/
        │   │           └── OrderCreatedConsumer.java     ← Event consumer
        │   └── resources/
        │       └── application.yml
        └── test/
```

---

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 | Primary language |
| **Spring Boot** | 3.2.3 | Application framework |
| **Spring Web** | (included) | REST controllers, RestTemplate |
| **Spring Data JPA** | (included) | ORM / H2 persistence |
| **Spring AMQP** | (included) | RabbitMQ integration |
| **H2 Database** | (runtime) | In-memory database per service |
| **RabbitMQ (CloudAMQP)** | AMQP 0-9-1 | Async event messaging |
| **Lombok** | (optional) | Boilerplate reduction |
| **Jackson** | (included) | JSON serialization of messages |
| **Maven** | 3.x | Build tool and dependency management |
| **Bean Validation (Jakarta)** | (included) | Request validation (`@Valid`, `@NotBlank`, `@Min`) |

---

## Prerequisites

Ensure the following are installed and configured before running the project:

| Requirement | Details |
|---|---|
| **Java 17+** | [Download JDK 17](https://adoptium.net/) — verify with `java -version` |
| **Maven 3.8+** | [Download Maven](https://maven.apache.org/download.cgi) — verify with `mvn -version` |
| **Git** | [Download Git](https://git-scm.com/) — for cloning the repository |
| **CloudAMQP account** | Free account at [cloudamqp.com](https://www.cloudamqp.com) — provides a hosted RabbitMQ instance |

> **Note:** Docker is **not required** to run this project locally. The H2 databases are embedded in each service.

---

## Clone the Repository

```bash
git clone https://github.com/stormbreaker3000/MicroServices-101-Order-Product.git
cd MicroApp
```

---

## Configuration

Both services require a live RabbitMQ broker. The easiest approach is a free [CloudAMQP](https://www.cloudamqp.com) instance.

### Step 1 – Create a CloudAMQP instance

1. Sign up at [cloudamqp.com](https://www.cloudamqp.com) and create a free **"Little Lemur"** plan instance.
2. From your instance dashboard, copy the following values:
   - **Host** (e.g., `collie.lmq.cloudamqp.com`)
   - **Port** (`5672`)
   - **Username**
   - **Password**
   - **Virtual Host**

### Step 2 – Update `application.yml` in both services

Open **`order-service/src/main/resources/application.yml`** and **`product-service/src/main/resources/application.yml`** and replace the placeholder values:

```yaml
spring:
  rabbitmq:
    host:         YOUR_HOST.cloudamqp.com   # e.g. collie.lmq.cloudamqp.com
    port:         5672
    username:     YOUR_USERNAME
    password:     YOUR_PASSWORD
    virtual-host: YOUR_VHOST
```

### Service Ports

| Service | Port | H2 Console URL |
|---|---|---|
| order-service | `8080` | http://localhost:8080/h2-console |
| product-service | `8081` | http://localhost:8081/h2-console |

### Product-Service Base URL (order-service only)

The order-service uses this setting to locate the product-service:

```yaml
# order-service/src/main/resources/application.yml
product-service:
  base-url: http://localhost:8081
```

Change this if the product-service runs on a different host or port.

---

## Running the Services

> **Important:** Start `product-service` **before** `order-service`, because the order-service makes a synchronous REST call to product-service at order creation time.

### Terminal 1 – Start product-service (port 8081)

```bash
cd product-service
mvn spring-boot:run
```

Wait until you see:
```
Started ProductServiceApplication in X.XXX seconds
```

### Terminal 2 – Start order-service (port 8080)

```bash
cd order-service
mvn spring-boot:run
```

Wait until you see:
```
Started OrderServiceApplication in X.XXX seconds
```

Both services are now running and connected to the shared RabbitMQ broker.

---

## Communication Flow

### Synchronous Communication (REST)

When a client creates an order, the following blocking call chain executes:

```
Client
  │
  │  POST /orders  { "productId": "1", "quantity": 2 }
  ▼
order-service (OrderController → OrderService → ProductClient)
  │
  │  GET http://localhost:8081/products/1
  ▼
product-service (ProductController → ProductService)
  │
  │  Returns: { "id": "1", "name": "Keyboard", "price": 49.99, "stock": 10 }
  ▼
order-service validates:
  - product exists ✓
  - stock (10) >= quantity (2) ✓
  → persists Order → publishes event → returns 201 Created
```

If the product is not found or stock is insufficient, the order is **rejected** with a `400 Bad Request` response.

### Asynchronous Communication (RabbitMQ)

After an order is persisted, the order-service publishes an event. The product-service processes it independently:

| Component | Value |
|---|---|
| **Exchange** | `orders.exchange` (type: topic, durable) |
| **Routing key** | `order.created` |
| **Queue** | `product.order.created.queue` (durable) |
| **Message format** | JSON (via Jackson) |

**Event payload (`OrderCreatedEvent`):**

```json
{
  "orderId":   "a1b2c3d4-...",
  "productId": "1",
  "quantity":  2,
  "createdAt": "2026-03-11T01:17:59Z"
}
```

**Flow:**

```
order-service
  └─ OrderEventPublisher.publish(event)
       └─ RabbitTemplate.convertAndSend(orders.exchange, order.created, event)
            └─→ [RabbitMQ broker]
                   └─→ product.order.created.queue
                          └─→ product-service
                                 └─ OrderCreatedConsumer.handleOrderCreated(event)
                                      └─ ProductService.reserveStock(event)
                                           └─ product.stock = product.stock - quantity
```

The product-service handles failures gracefully: if the product is not found or stock would go negative, a warning is logged and the event is discarded (no re-queue). For production use, a Dead Letter Queue (DLQ) is recommended.

---

## H2 Database

Each service uses its own independent **H2 in-memory database** — they do not share a data store.

| Property | order-service | product-service |
|---|---|---|
| JDBC URL | `jdbc:h2:mem:orderdb` | `jdbc:h2:mem:productdb` |
| Username | `sa` | `sa` |
| Password | *(empty)* | *(empty)* |
| H2 Console | http://localhost:8080/h2-console | http://localhost:8081/h2-console |
| DDL strategy | `create-drop` | `create-drop` |

> **Note:** `create-drop` means the schema is created when the service starts and **dropped when it stops**. All data is reset with each restart. Use the H2 console to inspect data at runtime or seed it manually.

### Data Initialization

There is no `data.sql` seed file — the databases start empty. You must create products via the REST API before placing orders.

---

## API Endpoints

### Product Service (`http://localhost:8081`)

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/products` | Retrieve all products | `200 OK` |
| `GET` | `/products/{id}` | Retrieve a product by its numeric ID | `200 OK`, `404 Not Found` |
| `POST` | `/products` | Create a new product | `201 Created`, `400 Bad Request` |

#### `POST /products` – Request Body

```json
{
  "name":  "Keyboard",
  "price": 49.99,
  "stock": 10
}
```

**Validation rules:**
- `name` — must not be blank
- `price` — must be greater than `0.00`
- `stock` — must be `0` or greater

#### `GET /products/{id}` – Response (200 OK)

```json
{
  "id":    "1",
  "name":  "Keyboard",
  "price": 49.99,
  "stock": 10
}
```

#### Product Not Found (404)

```json
{ "message": "Product not found" }
```

---

### Order Service (`http://localhost:8080`)

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/orders` | Retrieve all orders | `200 OK` |
| `GET` | `/orders/{orderId}` | Retrieve an order by UUID | `200 OK`, `404 Not Found` |
| `POST` | `/orders` | Create a new order | `201 Created`, `400 Bad Request` |

#### `POST /orders` – Request Body

```json
{
  "productId": "1",
  "quantity":  2
}
```

**Validation rules:**
- `productId` — must not be blank
- `quantity` — must be at least `1`

#### `POST /orders` – Response (201 Created)

```json
{
  "orderId":   "a1b2c3d4-5678-...",
  "productId": "1",
  "quantity":  2,
  "status":    "CREATED",
  "createdAt": "2026-03-11T01:17:59Z"
}
```

#### Error Responses (400 Bad Request)

| Scenario | Response Body |
|---|---|
| Product not found | `{ "message": "Product not found: <id>" }` |
| Insufficient stock | `{ "message": "Insufficient stock for product <id>: available X, requested Y" }` |
| `quantity` < 1 | `{ "errors": { "quantity": "quantity must be at least 1" } }` |
| `productId` blank | `{ "errors": { "productId": "productId must not be blank" } }` |

---

## How to Test the System

Follow these steps to exercise the complete synchronous + asynchronous flow:

### 1. Create a product

```bash
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Keyboard", "price": 49.99, "stock": 10}'
```

Note the returned `id` (e.g., `"1"`).

### 2. Verify the product exists

```bash
curl http://localhost:8081/products/1
```

Expected: `{ "id": "1", "name": "Keyboard", "price": 49.99, "stock": 10 }`

### 3. Create an order (triggers both REST + async flows)

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "1", "quantity": 2}'
```

Expected: HTTP `201` with the new order object and `"status": "CREATED"`.

At this point:
- order-service called product-service synchronously to validate stock ✓
- order-service published an `OrderCreatedEvent` to RabbitMQ ✓
- product-service consumed the event and reduced stock asynchronously ✓

### 4. Verify stock was reduced

```bash
curl http://localhost:8081/products/1
```

Expected: `"stock": 8` (reduced from 10 by 2).

### 5. Test error cases

```bash
# Insufficient stock (request more than available)
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "1", "quantity": 999}'
# → 400: "Insufficient stock for product 1: ..."

# Non-existent product
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "999", "quantity": 1}'
# → 400: "Product not found: 999"

# Invalid quantity
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "1", "quantity": 0}'
# → 400: validation error
```

---

## Troubleshooting

### RabbitMQ Connection Errors

**Symptom:** Service fails to start with `Connection refused` or `Authentication failure`.

**Fixes:**
- Verify your CloudAMQP credentials in `application.yml` are correct.
- Ensure your CloudAMQP instance is active and not paused (free-tier instances can pause after inactivity).
- Check that port `5672` is not blocked by a firewall.
- Confirm the `virtual-host` value matches exactly what CloudAMQP shows (it is usually the same as the username).

### Port Conflicts

**Symptom:** `Address already in use: bind` on port `8080` or `8081`.

**Fix:** Stop whatever process is using that port, or change the `server.port` in `application.yml`:

```yaml
# product-service/src/main/resources/application.yml
server:
  port: 9081   # change to any free port
```

If you change the product-service port, also update the `product-service.base-url` in the order-service config.

### H2 Data Not Persisting Between Restarts

This is **expected behavior**. The `create-drop` DDL strategy is designed for development. Data is lost on every restart. To preserve data across restarts, switch to a persistent database (PostgreSQL, MySQL, etc.) or change `ddl-auto` to `update`.

### Product-Service Not Reachable from Order-Service

**Symptom:** Order creation fails with `RuntimeException: Failed to reach product-service`.

**Fixes:**
- Confirm `product-service` is running on port `8081`.
- Ensure `product-service.base-url` in order-service's `application.yml` is set to `http://localhost:8081`.
- Check for firewalls or security tools blocking loopback connections.

### Stock Not Reduced After Order

**Symptom:** `GET /products/{id}` still shows the original stock value after placing an order.

**Fixes:**
- Wait 1–2 seconds; the async consumer processes events independently.
- Check the product-service logs for `Received OrderCreatedEvent` and `Stock reservation successful`.
- Verify both services connect to the **same** CloudAMQP instance (same host, vhost, and credentials).

---

## Future Improvements

| Improvement | Description |
|---|---|
| **Containerization** | Add `Dockerfile` for each service and a `docker-compose.yml` to spin up both services + a local RabbitMQ instance with a single command. |
| **Persistent Databases** | Replace H2 with PostgreSQL (or another RDBMS) so order and product data survive restarts. |
| **API Gateway** | Introduce Spring Cloud Gateway or NGINX to provide a single entry point, rate limiting, and routing to both services. |
| **Service Discovery** | Add Spring Cloud Eureka or Consul so services can find each other dynamically without hardcoded base URLs. |
| **Dead Letter Queue (DLQ)** | Configure a DLQ for `product.order.created.queue` so failed events are captured and can be replayed rather than silently discarded. |
| **Circuit Breaker** | Add Resilience4j to the `ProductClient` so the order-service degrades gracefully when the product-service is temporarily unreachable. |
| **Centralized Configuration** | Use Spring Cloud Config Server to manage configuration for all services from a single repository. |
| **Observability** | Add distributed tracing (Micrometer + Zipkin/Jaeger) and centralized logging (ELK stack) for end-to-end request visibility. |
| **Security** | Secure the endpoints with Spring Security + JWT tokens or OAuth2. |
| **Integration Tests** | Add `@SpringBootTest` tests using Testcontainers for RabbitMQ and a persistent database to verify the full request flow. |
