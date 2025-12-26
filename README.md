# URL Shortener Service

A production-ready, high-performance URL shortening and analytics service built with **Spring Boot 3**, **Java 21**, and **H2 Database**. This service provides REST APIs to create and manage short URLs, perform HTTP redirects, and collect comprehensive access statistics including geographic location, referrer, and user agent data. Features built-in **rate limiting**, **scheduled tasks**, and **observability** via **Spring Boot Actuator** and **Prometheus**.

---

## Features

- **URL Shortening**

  - Create short URLs with optional expiration dates and access limits
  - Bulk URL creation support
  - Update or delete existing short URLs
  - Paginated listing with configurable sorting (by created date, expiry, access limit)

- **Redirection**

  - Short code to long URL redirection with HTTP 302 redirects
  - Automatic request metadata capture for analytics (IP address, User-Agent, referrer, geographic information)

- **Analytics & Reporting**

  - Comprehensive access statistics grouped by:
    - URL
    - Country
    - City
    - Referrer
    - User-Agent
  - Paginated and sortable statistics endpoints

- **Resilience & Rate Limiting**

  - **Resilience4j** rate limiters configured for:
    - URL creation and management endpoints
    - Redirection endpoints
    - Request data (analytics) endpoints

- **Observability**

  - **Spring Boot Actuator** endpoints for health checks and metrics
  - **Prometheus** metrics export for monitoring

- **Developer Experience**
  - Clean layered architecture (Controller → Service → Repository → Model)
  - Global exception handling with consistent error responses
  - Async processing and scheduled tasks (e.g., URL expiration cleanup)
  - Dockerized for easy deployment

---

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **Build Tool**: Maven
- **Database**: H2 (in-memory, suitable for development/testing)
- **Persistence**: Spring Data JPA
- **Validation**: Jakarta Bean Validation / Spring Validation
- **Resilience**: Resilience4j (rate limiting)
- **Documentation**: springdoc-openapi (Swagger UI)
- **Metrics/Monitoring**: Spring Boot Actuator, Micrometer, Prometheus registry
- **Containerization**: Docker (multi-stage build using Eclipse Temurin JDK/JRE 21)

---

## Architecture Overview

### Entry Point

- `UrlShortenerApplication` - Spring Boot application class with scheduling enabled

### Core Layers

**Controller Layer** (`controller` package)

- `UrlController` - CRUD operations for short URLs (`/api/urls`, `/api/urls/add`, `/api/urls/update`, `/api/urls/delete`)
- `RedirectionController` - Handles short code to long URL redirection (`/r/{shortCode}`)
- `RequestDataController` - Access statistics APIs (`/api/request-data/stats`)

**Service Layer** (`service` and `service.impl` packages)

- `UrlService` / `UrlServiceImpl` - Business logic for URL creation, update, deletion, and listing
- `RedirectionService` / `RedirectionServiceImpl` - Short code lookup and request data logging
- `RequestDataService` / `RequestDataServiceImpl` - Analytics aggregation
- `GeoLookupService` / `GeoLookupServiceImpl` - Geographic information enrichment from IP addresses
- `AnalyticsService` - Higher-level analytics functions
- `UrlShortenerAlgorithm` - Core short code generation algorithm

**Persistence Layer** (`repository` package)

- `ShortUrlRepository` - JPA repository for `ShortUrl` entities
- `RequestDataRepository` - JPA repository for `RequestData` entities

**Domain & DTOs** (`model`, `dto`, `response`, `mapper` packages)

- `ShortUrl`, `RequestData` - Core domain entities
- DTOs: `CreateUrlDTO`, `UpdateUrlDTO`, `UrlAccessStatsDTO`, etc.
- Response types: `UrlResponse`, `RequestDataResponse`, `BulkUrlResult`, `ErrorResponse`
- Mappers for clean conversion between entities and DTOs

**Infrastructure**

- `config` - Configuration classes (Async, CORS, OpenAPI, RestTemplate)
- `UrlExpirationScheduler` - Scheduled task for cleaning up expired URLs
- `GlobalExceptionHandler` - Centralized error handling with consistent JSON responses

---

## Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- (Optional) **Docker** and **Docker Compose** for containerized runs
- A tool like `curl`, Postman, or a browser for testing the API

### Clone the Repository

```bash
git clone https://github.com/NelaniMaluka/url-shortener.git
cd url-shortner
```

### Build & Run (Maven)

```bash
# Build (runs tests by default)
mvn clean package

# Run (dev profile using in-memory H2)
mvn spring-boot:run
```

The service will start on **`http://localhost:8080`** by default.

---

## Configuration

All configuration is in `src/main/resources/application.properties` for the default profile.

### Key Properties

**Core**

- `spring.application.name=url-shortner`
- `server.port=8080`

**Rate Limiting (Resilience4j)**

- **Shorten URL endpoints** (e.g., `/api/urls`, `/api/urls/add`, `/api/urls/update`, `/api/urls/delete`):

  - `resilience4j.ratelimiter.instances.shortenRateLimiter.limit-for-period=20`
  - `resilience4j.ratelimiter.instances.shortenRateLimiter.limit-refresh-period=1s`
  - `resilience4j.ratelimiter.instances.shortenRateLimiter.timeout-duration=0`

- **Redirect endpoints** (`/r/{shortCode}`):

  - `resilience4j.ratelimiter.instances.redirectRateLimiter.limit-for-period=100`
  - `resilience4j.ratelimiter.instances.redirectRateLimiter.limit-refresh-period=1s`
  - `resilience4j.ratelimiter.instances.redirectRateLimiter.timeout-duration=0`

- **Request Data (analytics) endpoints** (`/api/request-data/**`):
  - `resilience4j.ratelimiter.instances.requestDataRateLimiter.limit-for-period=30`
  - `resilience4j.ratelimiter.instances.requestDataRateLimiter.limit-refresh-period=1s`
  - `resilience4j.ratelimiter.instances.requestDataRateLimiter.timeout-duration=0`

**Observability**

- `management.endpoints.web.exposure.include=health,info,prometheus,metrics`
- `management.endpoint.metrics.enabled=true`
- `management.endpoint.prometheus.enabled=true`
- `management.endpoint.health.show-details=always`

> **Note for Production**: For production deployments, you should:
>
> - Switch to a persistent database (PostgreSQL, MySQL, etc.)
> - Reduce logging verbosity from `DEBUG` to `INFO`/`WARN`
> - Adjust rate limits based on expected traffic
> - Use profile-specific configuration (e.g., `application-prod.properties`)

---

## Running with Docker

This project includes a **multi-stage Dockerfile**:

- **Build stage**: Uses `eclipse-temurin:21`, installs Maven, downloads dependencies, and runs `mvn clean package -DskipTests`
- **Runtime stage**: Uses `eclipse-temurin:21-jre`, copies the built JAR, and runs as a **non-root** user

### Build the Image

```bash
docker build -t url-shortner:latest .
```

### Run the Container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JAVA_OPTS="-Xms256m -Xmx512m" \
  --name url-shortner \
  url-shortner:latest
```

The application will be available at **`http://localhost:8080`**.

---

## API Overview

### URL Management (`UrlController`)

Base path: `/api`

**List URLs**

- **GET** `/api/urls`
- **Query Parameters:**
  - `page` (default: `0`)
  - `size` (default: `10`)
  - `sortBy` (`CREATED_AT`, `EXPIRES_AT`, `ACCESS_LIMIT`; default: `CREATED_AT`)
  - `direction` (`ASC` or `DESC`; default: `DESC`)
- **Rate Limiter**: `shortenRateLimiter`
- **Response**: Paginated list of URL records

**Create Short URLs (Bulk)**

- **POST** `/api/urls/add`
- **Body**: `List<CreateUrlDTO>` (each containing the long URL and optional configuration like expiry, access limit, etc.)
- **Rate Limiter**: `shortenRateLimiter`
- **Response**: `List<BulkUrlResult>` (success or error per input item)

**Update Existing URL**

- **PUT** `/api/urls/update`
- **Body**: `UpdateUrlDTO` (contains original URL/short code and new target URL)
- **Rate Limiter**: `shortenRateLimiter`
- **Response**: `UrlResponse` (updated mapping)

**Delete URL**

- **DELETE** `/api/urls/delete`
- **Body**: Plain `String` representing the URL or identifier to delete
- **Rate Limiter**: `shortenRateLimiter`
- **Response**: `204 No Content`

### Redirection (`RedirectionController`)

Base path: `/r`

**Redirect Short URL**

- **GET** `/r/{shortCode}`
- **Path Variable:**
  - `shortCode` - The generated short code (e.g., `a8f3Ks`)
- **Rate Limiter**: `redirectRateLimiter`
- **Behavior:**
  - Looks up the long URL
  - Records request data (IP, user agent, referrer, geo info, etc.)
  - Returns **HTTP 302** redirect with `Location` header set to the long URL

### Analytics & Request Data (`RequestDataController`)

Base path: `/api/request-data`

**Access Statistics**

- **GET** `/api/request-data/stats`
- **Query Parameters:**
  - `groupBy` (`URL`, `COUNTRY`, `CITY`, `REFERRER`, `USER_AGENT`, etc.; default: `COUNTRY`)
  - `page` (default: `0`, `@Min(0)`)
  - `size` (default: `10`, `@Max(100)`)
  - `direction` (`ASC` or `DESC`; default: `DESC`)
- **Response**: `Page<UrlAccessStats>` with aggregated statistics

---

## API Documentation (Swagger / OpenAPI)

The controllers are annotated with **OpenAPI** annotations (`@Operation`, `@ApiResponse`, etc.).  
Using **springdoc-openapi**, a Swagger UI is typically exposed at:

- `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

> If you change context path or management settings, the exact URLs may differ.

---

## Observability & Monitoring

With Actuator and Micrometer Prometheus registry configured, you get:

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus Scrape Endpoint**: `GET /actuator/prometheus`

You can hook Prometheus and Grafana to visualize traffic, latency, error rates, and other application-level metrics.

---

## Testing

The project includes a comprehensive test suite under `src/test/java/com/nelani/url_shortner`:

- **Controller Tests**: `UrlControllerTest`, `RedirectionControllerTest`, `RequestDataControllerTest`
- **Service Tests**: `UrlServiceTest`, `RedirectionServiceTest`, `RequestDataServiceTest`, `AnalyticsServiceTest`, `GeoLookupServiceTest`, `UrlShortenerAlgorithmTest`
- **Repository Tests**: `ShortUrlRepositoryTest`, `RequestDataRepositoryTest`
- **Application Context**: `UrlShortenerApplicationTests`

Run all tests with:

```bash
mvn test
```

---

## License

[Add your license here - e.g., MIT, Apache 2.0, etc.]

---
