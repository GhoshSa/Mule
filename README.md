# Mule

An event-driven order processing system built as a learning project — implementing
the saga pattern, atomic concurrency control, and package-by-feature architecture
in Spring Boot, with the explicit goal of eventually splitting into real
microservices.

Mule is intentionally scoped as a **modular monolith MVP** right now. It
demonstrates the core coordination patterns used in real distributed
e-commerce order systems (orchestrated sagas, eventual consistency, atomic
inventory reservation) without yet paying the operational cost of actual
network boundaries between services — that split is the planned next
milestone, not a missing feature of this one.

## Overview

Placing an order is a two-step process: create the order, then reserve stock
for it. These two steps can each fail independently and — once inventory
becomes its own service — can't share a single database transaction. Mule
coordinates them with an **orchestrated saga**: a dedicated component runs
each step in its own committed transaction and moves the order to a terminal
state based on the outcome, rather than relying on one all-or-nothing
transaction.

```
Client --POST /orders--> OrderController
                              |
                              v
                     OrderSagaOrchestrator
                        /            \
                       v              v
                 OrderService    InventoryService
                  (order pkg)     (inventory pkg)
                       |               |
                       v               v
                    Orders table   InventoryItems table
```

## Architecture

Mule is a **package-by-feature modular monolith**: `order` and `inventory`
are self-contained — each owns its model, repository, service, and (for
`order`) controller and DTOs — and neither reaches into the other's
internals directly. Cross-domain coordination lives in its own top-level
package (`saga`), so it's explicit which code is responsible for wiring
domains together versus owning a domain outright.

```
io.github.ghoshsa.mule
├── order/
│   ├── controller/   OrderController
│   ├── dto/          CreateOrderRequest, OrderItemRequest, OrderResponse
│   ├── model/        Order, OrderItem, OrderStatus
│   ├── repository/   OrderRepository
│   ├── service/      OrderService
│   └── exception/    OrderNotFoundException
├── inventory/
│   ├── model/         InventoryItem
│   ├── repository/    InventoryRepository
│   ├── service/       InventoryService
│   └── DataSeeder      (dev-only stock seeding)
├── saga/
│   └── OrderSagaOrchestrator
└── common/
    └── web/            ErrorResponse, GlobalExceptionHandler
```

This structure is deliberate, not incidental: because each feature package is
self-contained, extracting `inventory` into its own deployable service later
is a matter of moving one folder and swapping an in-process call for a
network call — not untangling code that was never separated in the first
place.

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

A Postman collection covering both the success and
insufficient-stock paths is included for manual testing.

## Running locally

```bash
./mvnw spring-boot:run
```

Runs on port `8080` against an in-memory H2 database (Postgres is the
target for the next milestone — see below). On startup, `DataSeeder` seeds
two products for manual testing: one with stock, one with zero stock, so
both the success and business-failure paths are reachable immediately.

The H2 console is available at `/h2-console` for inspecting table state
directly during development.

## Testing

```bash
./mvnw test
```

- `SystemConcurrencyTest` — fires 12 concurrent order requests at 10 units
  of stock using `CountDownLatch`-synchronized threads, and asserts the
  exact confirmed/failed split and final stock level.

## Known limitations (current MVP)

These are deliberate, documented scope cuts — not oversights discovered
after the fact:

- **Single item per order only.** Multi-item orders would hit a real bug:
  if an order's second item fails to reserve after the first succeeded,
  nothing releases the first item's reservation, permanently and silently
  losing that stock. Restricting to one item removes the precondition for
  this bug entirely until proper compensation logic is built.
- **No idempotency key support yet.** A client retry can currently create a
  duplicate order.
- **No business-vs-technical failure classification yet.** A genuine
  out-of-stock condition and a transient database/service failure currently
  look identical to the saga — both simply return `false` from
  `reserveStock()`.
- **Sequential auto-increment IDs, not UUIDs.** Fine for a single-database
  monolith; will need to change once `inventory` has its own database.
- **The global exception handler covers `OrderNotFoundException` only.**
  Validation failures and unexpected exceptions do not yet have dedicated
  handlers in the shipped version — they currently fall through to Spring's
  default error handling rather than the project's structured
  `ErrorResponse` envelope.
- **On H2, not Postgres.** H2 is used as a local development stand-in;
  the data model and queries are written to be Postgres-compatible.

## Next milestone: the microservices split

The next phase is extracting `inventory` into a genuinely separate
deployable service, in a deliberately staged sequence rather than all at
once — each stage is real, separable engineering work, and building them
together with a fresh network boundary would make it hard to isolate the
source of any bug that appears:

1. **Split first, alone.** Multi-module Maven repo, separate Postgres
   database per service via Docker Compose, REST calls between them. No new
   saga sophistication yet.
2. **Idempotency keys** — becomes genuinely necessary
   once retries over a real network are possible.
3. **Retries with timeouts** — safe to add only once idempotency is
   already in place underneath them.
4. **Durable saga state** — persisting orchestrator progress so a crashed
   orchestrator can resume rather than just react; motivated by having felt
   the pain of retry-related crashes.
5. **Compensation logic** — releasing already-reserved stock when a later
   step fails, unlocking multi-item orders. Saved for last deliberately,
   since it's the most complex piece and benefits from everything below it
   already being solid.

API contracts may be redesigned during this split rather than preserving
the current MVP's exact request/response shape — documented here explicitly
so that isn't a silent, unplanned drift later.