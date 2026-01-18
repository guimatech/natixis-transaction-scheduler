# Transaction Scheduler API

REST API for scheduling banking transactions with automatic fee calculation, built with **Java 17**, **Spring Boot** and **Clean Architecture** principles.

Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

---

## 1. How to Run

### Prerequisites

- Java 17+
- Maven (or `mvnw` wrapper included in the project)
- Port `8080` available

### Start application and open Swagger UI

A helper script `run.sh` is provided to build and run the application and open Swagger UI automatically.

```bash
./run.sh
```

This script will:

- Build the project with Maven (skipping tests by default)
- Start the Spring Boot application on port `8080`
- Open Swagger UI at  
  `http://localhost:8080/api/swagger-ui/index.html`

If the browser does not open automatically, access the URL manually.

---

## 2. Architecture

The project follows **Clean Architecture / Hexagonal Architecture** principles.

### Domain
- Core business entities and value objects
- Fee rules and transaction model
- Ports (input/output interfaces)

### Application
- Use cases implementing business workflows:
  - `CreateTransactionUseCase`
  - `GetTransactionUseCase`
  - `UpdateTransactionUseCase`
  - `DeleteTransactionUseCase`

### Infrastructure
- REST controllers (adapters)
- DTOs and mappers
- Persistence adapters (repositories)
- Exception handlers

This separation keeps business logic independent from frameworks and I/O details.

---

## 3. Business Rules

For every scheduled transaction, a transfer fee is charged according to the rules below.

### Fee A
- Amount between **0 and 1000 EUR**
- Scheduled date **equal to current date**
- Fee: **3% of transfer amount + 3 EUR**

### Fee B
- Amount between **1001 and 2000 EUR**
- Scheduled date **1 to 10 days** after current date
- Fee: **9% of transfer amount**

### Fee C
- Amount **greater than 2000 EUR**

| Days After Current Date | Fee |
|-------------------------|-----|
| 11–20 days | 8.2% |
| 21–30 days | 6.9% |
| 31–40 days | 4.7% |
| More than 40 days | 1.7% |

If no fee configuration matches a given combination of amount and days, the API returns **HTTP 404** with a clear error message.

---

## 4. API Endpoints

Base path: `/v1/transactions`

All endpoints are documented in Swagger UI:  
`http://localhost:8080/api/swagger-ui/index.html`

---

### 4.1 Create Transaction

**POST** `/v1/transactions`

Creates a new scheduled transaction and automatically calculates the fee.

#### Request Body

```json
{
  "sourceAccount": "PT50000201231234567890154",
  "destinationAccount": "DE89370400440532013000",
  "transferAmount": 3500,
  "scheduledDate": "2026-01-30"
}
```

#### Responses
- `201 Created` – transaction created successfully
- `400 Bad Request` – validation errors
- `404 Not Found` – no matching fee configuration
- `500 Internal Server Error` – unexpected error

---

### 4.2 Get Transaction by ID

**GET** `/v1/transactions/{id}`

- `200 OK` – transaction found
- `404 Not Found` – transaction not found

---

### 4.3 Get All Transactions

**GET** `/v1/transactions`

- `200 OK` – returns a list (can be empty)

---

### 4.4 Get Transactions by Scheduled Date

**GET** `/v1/transactions/scheduled/{date}`

- `200 OK` – list of transactions for the given date

---

### 4.5 Get Transactions by Source Account

**GET** `/v1/transactions/accounts/{accountNumber}`

- `200 OK` – list of transactions for the source account

---

### 4.6 Update Transaction (Full)

**PUT** `/v1/transactions/{id}`

Updates all transaction fields and recalculates the fee if needed.

- `200 OK`
- `400 Bad Request`
- `404 Not Found`

---

### 4.7 Partial Update Transaction

**PATCH** `/v1/transactions/{id}`

Updates only provided fields. Fee is recalculated when amount or date changes.

- `200 OK`
- `400 Bad Request`
- `404 Not Found`

---

### 4.8 Delete Transaction

**DELETE** `/v1/transactions/{id}`

- `204 No Content`
- `404 Not Found`

---

## 5. Example Swagger Payloads

### Fee A (today, 0–1000)

```json
{
  "sourceAccount": "PT50000201231234567890154",
  "destinationAccount": "DE89370400440532013000",
  "transferAmount": 500,
  "scheduledDate": "2026-01-18"
}
```

### Fee B (1–10 days, 1001–2000)

```json
{
  "sourceAccount": "FR7630006000011234567890189",
  "destinationAccount": "ES9121000418450200051332",
  "transferAmount": 1500,
  "scheduledDate": "2026-01-23"
}
```

### Fee C (>2000, 11–20 days)

```json
{
  "sourceAccount": "PT50000201231234567890154",
  "destinationAccount": "DE89370400440532013000",
  "transferAmount": 3500,
  "scheduledDate": "2026-01-30"
}
```

---

## 6. Tests

Run tests with:

```bash
mvn test
```

Tests cover:
- Successful creation with Fee A, B and C
- Validation errors
- Missing fee configurations

---

## 7. Technologies

- Java 17
- Spring Boot (Web, Validation, Data Access)
- springdoc-openapi (Swagger UI)
- Maven
- H2 (tests)
- Clean / Hexagonal Architecture
