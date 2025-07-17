# Buy Recipes API Challenge

A simple API for managing recipes and carts.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running the Application](#running-the-application)
- [API Usage & Documentation](#api-usage--documentation)
- [Running Tests](#running-tests)
- [Observability](#observability)

---

## Tech Stack

-   **Framework**: Spring Boot 3.3
-   **Language**: Kotlin 1.9
-   **Database**: PostgreSQL
-   **Build Tool**: Gradle
-   **API Documentation**: Springdoc OpenAPI
-   **Testing**: JUnit 5, Mockito, Testcontainers
-   **Database Migrations**: Flyway

---

## Getting Started
### Prerequisites

- Docker
- Docker Compose

### Running the Application

1.  **Create Environment File**

    Create a `.env` file in the project root by copying the example below. This provides the credentials for the local database container.

    ```
    # .env
    POSTGRES_USER=user
    POSTGRES_PASSWORD=password
    ```

2.  **Build and Run**

    Use Docker Compose to build the application image and start the service and its database.

    ```bash
    docker-compose up --build
    ```

The API will be available at `http://localhost:8080`.

---

## API Usage & Documentation

An interactive Swagger UI is available for exploring and testing the API endpoints.

-   **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

#### Key Endpoints:

-   `GET /recipes`: List all available recipes.
-   `GET /carts/{id}`: Get a cart by its ID.
-   `POST /carts/{cartId}/add_recipe`: Add a recipe to a cart.
-   `DELETE /carts/{cartId}/recipes/{recipeId}`: Remove a recipe from a cart.

---

## Running Tests

To run all tests, use the Gradle wrapper:

```bash
./gradlew test
```

An HTML report of the test results is generated in *build/reports/tests/test/index.html*.


## Observability

The service includes standard actuator endpoints for monitoring.
-   Health Check: http://localhost:8080/actuator/health
-   Prometheus Metrics: http://localhost:8080/actuator/prometheus

Structured (JSON) logging is automatically enabled when the application is run with the prod Spring profile.