# mv-event-ledger

Minimum viable Event Ledger with two Spring Boot 3 services and a shared domain module.

## Modules

- `event-ledger-domain`: shared DTOs, enums, JPA entities, repositories, validation annotations, Swagger schema annotations, Liquibase SQL changelogs.
- `event-gateway-api`: public API for event submission and event lookup.
- `account-service`: internal account transaction and balance service.

## Run Locally

Start Account Service first:

```powershell
mvn -pl account-service -am spring-boot:run
```

Start Gateway in another terminal:

```powershell
mvn -pl event-gateway-api -am spring-boot:run
```

Run with Docker Compose:

```powershell
docker compose up --build
```

## API Documentation

- Gateway Swagger UI: `http://localhost:8080/swagger-ui.html`
- Gateway OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Account Service Swagger UI: `http://localhost:8081/swagger-ui.html`
- Account Service OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Main Endpoints

Gateway:

- `POST /events`
- `GET /events/{id}`
- `GET /events?account={accountId}`
- `GET /accounts/{accountId}/balance`
- `GET /accounts/{accountId}`
- `GET /health`

Account Service:

- `POST /accounts`
- `POST /accounts/{accountId}/transactions`
- `GET /accounts/{accountId}/balance`
- `GET /accounts/{accountId}`
- `GET /health`

## Implemented MVP Behavior

- Idempotency via unique `event_id` in gateway event storage and account transactions.
- Events list ordered by `eventTimestamp`.
- Balance is maintained by account service as credits minus debits.
- Gateway calls Account Service synchronously using `RestTemplate`.
- Gateway validates account IDs through Account Service before applying new events.
- Gateway has Resilience4j circuit breaker, retry, and rate limiter around Account Service calls.
- Gateway keeps successful account validations in a bounded in-memory LRU cache.
- `X-Trace-Id` is generated or propagated across Gateway to Account Service.
- JSON-style structured logs include timestamp, level, service, trace ID, logger, and message.
- Gateway `/health` includes local database and `account-service` dependency status.
- Liquibase uses formatted SQL changelogs with service-specific contexts: `gateway` and `account`.

## Tests

Run all tests:

```powershell
mvn test
```
## ToDo 

Authentication and authorization is to be implemented.
Need to add more test cases