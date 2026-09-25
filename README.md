# Mule

Mule is a learning project for an event-driven order flow systems. It uses the saga pattern and a package-by-feature structure in Spring Boot, with the long-term goal of splitting into real distributed system.

Right now, it is intentionally a modular monolith MVP while it works its way toward that split. The project demonstrates the coordination patterns used in real e-commerce systems: orchestrated sagas, eventual consistency, and atomic inventory reservation. It has also taken its first real step toward a physical service split by separating the build into modules before changing any network or database boundaries.

## Overview

Placing an order is a two-step process: create the order, then reserve the stock for it. Each step can fail on its own, and once inventory becomes a separate service, they cannot share a single database transaction. Mule coordinates the work with an orchestrated saga: a dedicated component runs each step in its own committed transaction and then moves the order into a terminal state based on the outcome, instead of relying on one big all-or-nothing transaction.

```
Client --POST /orders--> app/controller/OrderController
                                    |
                                    v
                        app/saga/OrderSagaOrchestrator
                              /            \
                             v              v
                       OrderService    InventoryService
                     (order-service)  (inventory-service)
                            |                |
                            v                v
                      Orders table     InventoryItems table
```

## Architecture

Mule is now a **multi-module Maven build**, structured to mirror the services it will eventually become:

```
mule/                        (parent — packaging=pom, no source code)
├── order-service/           (jar — pure order domain, no dependency on inventory-service)
│   └── io.github.ghoshsa.mule.order/
│       ├── model/           Order, OrderItem, OrderStatus
│       ├── repository/      OrderRepository
│       ├── service/         OrderService
│       ├── exception/       OrderNotFoundException
│       └── dto/             CreateOrderRequest, OrderItemRequest, OrderResponse
├── inventory-service/       (jar — pure inventory domain, no dependency on order-service)
│   └── io.github.ghoshsa.mule.inventory/
│       ├── model/           InventoryItem
│       ├── repository/      InventoryRepository
│       ├── service/         InventoryService
│       └── DataSeeder       (dev-only stock seeding)
└── app/                     (jar — the runnable Spring Boot app; composes the above)
    └── io.github.ghoshsa.mule/
        ├── MuleApplication.java
        ├── saga/OrderSagaOrchestrator.java
        ├── controller/OrderController.java
        └── common/web/      ErrorResponse, GlobalExceptionHandler
```

`order-service` and `inventory-service` deliberately have **no dependency on each other** — that's the actual point of the split, verified at build time, not just by folder convention. `app` depends on both, and is the only module that knows both domains exist.

This structure is what makes the next step — giving `inventory-service` its own real deployable process and database, and replacing the in-process call with a REST call — a straightforward extraction task instead of a big refactor of code.

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Places an order (single item only in this MVP). Returns the order with its saga outcome (`CONFIRMED` or `FAILED`). |
| `GET` | `/orders/{id}` | Fetches an order by ID. Returns `404` with a structured error body if not found. |

Example request:
```json
POST /orders
{
  "customerId": "customer-1",
  "items": [{"productId": "product-1", "quantity": 1}]
}
```

Example error response (404):
```json
{
  "status": 404,
  "error": "ORDER_NOT_FOUND",
  "message": "Order not found: 999",
  "timeStamp": "2026-09-14T10:00:00Z",
  "path": "/orders/999"
}
```

A Postman collection covering both the success and insufficient-stock paths is included for manual testing.

## Running locally

```bash
./mvnw clean install
./mvnw -pl app spring-boot:run
```

The `-pl app` flag is now required — with three modules in the reactor, Maven needs to be told which one is actually runnable.

Runs on port `8080` against an in-memory H2 database (Postgres per service, via Docker Compose, is the next step of the milestone — see below). On startup, `DataSeeder` seeds two products for manual testing: one with stock, one with zero stock, so both the success and business-failure paths are reachable immediately.

The H2 console is available at `/h2-console` for inspecting table state directly during development.

## Testing

```bash
./mvnw test
```

- `SystemConcurrencyTest` — fires 12 concurrent order requests at 10 units
  of stock using `CountDownLatch`-synchronized threads, and asserts the
  exact confirmed/failed split and final stock level.

Tests live in `app`, since they exercise the fully composed application (real HTTP layer, real orchestrator both domains wired together) — the same place `OrderController` and the orchestrator now live.

## Known limitations (current MVP)

These are deliberate, documented scope cuts — not oversights discovered
after the fact:

- **Single item per order only.** Multi-item orders would hit a real bug:
  if an order's second item fails to reserve after the first succeeded, nothing releases the first item's reservation, permanently and silently losing that stock. Restricting to one item removes the precondition for this bug entirely until proper compensation logic is built.
- **No idempotency key support yet.** A client retry can currently create a duplicate order.
- **No business-vs-technical failure classification yet.** A genuine out-of-stock condition and a transient database/service failure currently look identical to the saga — both simply return `false` from
  `reserveStock()`.
- **Sequential auto-increment IDs, not UUIDs.** Fine for now; will need to change once `inventory-service` has its own database.
- **The global exception handler covers `OrderNotFoundException` only.** Validation failures and unexpected exceptions do not yet have dedicated handlers in the shipped version.
- **Still on H2, one process.** The build is now split into modules that mirror future services, but at runtime it's still a single app process against a single in-memory database — the actual network and database boundary hasn't been introduced yet (next step, below).

## Next milestone: the microservices split

Extracting `inventory-service` into a genuinely separate deployable process, in a deliberately staged sequence — each stage is real, separable engineering work, and building them together with a fresh network boundary would make it hard to isolate the source of any bug that appears:

1. **Split first, alone** — in progress.
   - ✅ Multi-module Maven build: `order-service` and `inventory-service` have zero dependency on each other, verified at compile time.
   - ⏳ Separate Postgres database per service via Docker Compose (H2 is still shared/in-process for now).
   - ⏳ Replace `OrderSagaOrchestrator`'s in-process call to `InventoryService` with a real REST call between two separately running processes.
   - Once both of those land, the goal is to confirm the saga's behavior — `PENDING` → `CONFIRMED`/`FAILED` — still holds correctly across a real network hop and two real, separate databases, with API responses otherwise unchanged from the current MVP.
2. **Idempotency keys** — becomes genuinely necessary once retries over a real network are possible.
3. **Retries with timeouts** — safe to add only once idempotency is already in place underneath them.
4. **Durable saga state** — persisting orchestrator progress so a crashed orchestrator can resume rather than just react; motivated by having felt the pain of retry-related crashes.
5. **Compensation logic** — releasing already-reserved stock when a later step fails, unlocking multi-item orders. Saved for last deliberately, since it's the most complex piece and benefits from everything below it already being solid.

API contracts may be redesigned during this split rather than preserving the current MVP's exact request/response shape — documented here explicitly so that isn't a silent, unplanned drift later.