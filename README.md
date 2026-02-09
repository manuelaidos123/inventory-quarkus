# Inventory Quarkus API

A RESTful inventory management microservice built with Quarkus, providing CRUD operations for inventory items with pagination, validation, and OpenAPI documentation.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Data Model](#data-model)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Technology Stack](#technology-stack)

## Features

- ✅ Full CRUD operations for inventory items
- ✅ Paginated list endpoint with metadata
- ✅ Search inventory by product ID
- ✅ Bean Validation for input data
- ✅ OpenAPI/Swagger UI documentation
- ✅ Health checks (liveness and readiness)
- ✅ Comprehensive error handling with consistent error responses
- ✅ Caching with Caffeine for improved performance
- ✅ H2 in-memory database for development
- ✅ Native image compilation support

## Prerequisites

- Java 17+
- Maven 3.8+
- (Optional) GraalVM for native compilation

## Running the Application

### Development Mode

```bash
./mvnw quarkus:dev
```

The application will start at `http://localhost:8080`.

### Production Mode

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Mode

```bash
./mvnw package -Dnative
./target/inventory-quarkus-1.0.0-SNAPSHOT-runner
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **OpenAPI Spec (YAML)**: http://localhost:8080/q/openapi
- **OpenAPI Spec (JSON)**: http://localhost:8080/q/openapi?format=json

## API Endpoints

### List Inventory Items (Paginated)

```http
GET /api/inventory?page=0&size=20
```

**Response:**
```json
{
  "data": [
    {"id": 100000, "productId": 1001, "quantity": 0},
    {"id": 165613, "productId": 1004, "quantity": 45}
  ],
  "total": 8,
  "page": 0,
  "size": 20,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | Page number (0-based) |
| size | int | 20 | Page size (max 100) |

### List All Inventory Items (No Pagination)

```http
GET /api/inventory/all
```

### Get Inventory Count

```http
GET /api/inventory/count
```

**Response:** `8` (text/plain)

### Get Inventory by ID

```http
GET /api/inventory/{itemId}
```

**Response:**
```json
{
  "id": 329299,
  "productId": 1002,
  "quantity": 35
}
```

### Get Inventory by Product ID

```http
GET /api/inventory/product/{productId}
```

**Response:**
```json
{
  "id": 329299,
  "productId": 1002,
  "quantity": 35
}
```

### Create Inventory Item

```http
POST /api/inventory
Content-Type: application/json

{
  "productId": 9999,
  "quantity": 100
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "productId": 9999,
  "quantity": 100
}
```

**Note:** The `id` field is auto-generated. Do not include it in the request body.

### Update Inventory Item (Full Update)

```http
PUT /api/inventory/{itemId}
Content-Type: application/json

{
  "productId": 9999,
  "quantity": 200
}
```

**Response:** `200 OK`

### Update Quantity (Partial Update)

```http
PATCH /api/inventory/{itemId}/quantity
Content-Type: application/json

{
  "quantity": 500
}
```

**Response:** `200 OK`

### Delete Inventory Item

```http
DELETE /api/inventory/{itemId}
```

**Response:** `204 No Content`

### Clear All Caches

```http
DELETE /api/inventory/cache
```

**Response:** `204 No Content`

Clears all cached inventory data. Useful for administrative purposes or when you need to force a cache refresh.

### Health Checks

```http
GET /q/health           # Overall health
GET /q/health/ready     # Readiness probe
GET /q/health/live      # Liveness probe
```

## Data Model

### Inventory

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Auto-generated | Unique identifier |
| productId | Long | Required, Unique | Associated product ID |
| quantity | int | Required, Min 0 | Available quantity |

### Example JSON

```json
{
  "id": 329299,
  "productId": 1002,
  "quantity": 35
}
```

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Inventory item not found with id: 999999",
  "path": "/api/inventory/999999",
  "timestamp": "2026-02-09T00:00:00.000Z"
}
```

### HTTP Status Codes

| Status | Description |
|--------|-------------|
| 200 | Success (GET, PUT, PATCH) |
| 201 | Created (POST) |
| 204 | No Content (DELETE) |
| 400 | Bad Request - Validation error |
| 404 | Not Found - Resource doesn't exist |
| 415 | Unsupported Media Type |
| 500 | Internal Server Error |

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify -Dnative
```

### Test Categories

- **Unit Tests**: `InventoryResourceTest.java` (30 tests)
- **Native Tests**: `NativeInventoryResourceIT.java`

## Technology Stack

- **Framework**: Quarkus 3.8.4
- **Language**: Java 17
- **JAX-RS**: RESTEasy Reactive
- **ORM**: Hibernate ORM with Panache
- **Database**: H2 (dev), configurable for PostgreSQL/MySQL
- **Validation**: Hibernate Validator
- **Documentation**: SmallRye OpenAPI with Swagger UI
- **Health**: SmallRye Health
- **Caching**: Quarkus Cache with Caffeine
- **Testing**: JUnit 5, Rest Assured

## Project Structure

```
src/
├── main/
│   ├── java/com/redhat/cloudnative/
│   │   ├── Inventory.java              # Entity class
│   │   ├── InventoryResource.java      # REST endpoints
│   │   ├── PaginatedResponse.java      # Pagination wrapper
│   │   ├── QuantityUpdateRequest.java  # DTO for PATCH
│   │   ├── ErrorResponse.java          # Error response DTO
│   │   ├── InventoryNotFoundException.java
│   │   ├── InventoryNotFoundExceptionMapper.java
│   │   ├── InvalidInventoryException.java
│   │   ├── InvalidInventoryExceptionMapper.java
│   │   └── ConstraintViolationExceptionMapper.java
│   └── resources/
│       ├── application.properties      # Configuration
│       └── import.sql                  # Seed data
└── test/
    └── java/com/redhat/cloudnative/
        ├── InventoryResourceTest.java
        └── NativeInventoryResourceIT.java
```

## Caching

This application uses Quarkus Cache with Caffeine backend for improved performance on read operations.

### Cached Endpoints

| Endpoint | Cache Name | Description |
|----------|------------|-------------|
| `GET /api/inventory/{itemId}` | `inventory-cache` | Cached by inventory ID |
| `GET /api/inventory/product/{productId}` | `inventory-product-cache` | Cached by product ID |

### Cache Invalidation

Caches are automatically invalidated on data modifications:

| Operation | Cache Behavior |
|-----------|----------------|
| POST (create) | All caches cleared |
| PUT (update) | Specific ID + product cache cleared |
| PATCH (update quantity) | Specific ID + product cache cleared |
| DELETE | Specific ID + product cache cleared |

### Cache Configuration

```properties
# Cache expires after 5 minutes of being written
quarkus.cache.caffeine.inventory-cache.expire-after-write=5m
quarkus.cache.caffeine.inventory-product-cache.expire-after-write=5m
```

### Manual Cache Clear

Use the `DELETE /api/inventory/cache` endpoint to manually clear all caches.

## Configuration

Key configuration options in `application.properties`:

```properties
# Database (H2 in-memory for development)
quarkus.datasource.jdbc.url=jdbc:h2:mem:inventory
quarkus.datasource.db-kind=h2

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql

# Cache Configuration (Caffeine backend)
quarkus.cache.caffeine.inventory-cache.expire-after-write=5m
quarkus.cache.caffeine.inventory-product-cache.expire-after-write=5m
```

## License

This project is licensed under the Apache License 2.0.