# Visit Service

## Project Title & Description
**Name:** Visit Service  
**Bounded Context:** Pet Care & Scheduling Domain (Spring PetClinic Microservices)

The Visit Service is a specialized microservice within the PetClinic ecosystem responsible for managing and persisting pet visit records. Operating within the Pet Care & Scheduling bounded context, it handles the lifecycle of visits, associating them with specific pets, and ensuring valid state transitions. It integrates seamlessly with the Customer/Ownership Service to validate pet existence and synchronize cross-domain data.

## Tech Stack
* **Java 21**: Modern, performant Java version providing virtual threads and enhanced pattern matching.
* **Spring Boot 3.2.x**: Core framework for robust, cloud-native RESTful application development.
* **Spring Data JPA & H2**: Data access layer and lightweight in-memory database for rapid development and testing.
* **Spring Cloud Netflix Eureka**: Service discovery client for dynamic routing and load balancing within the microservice cluster.
* **RabbitMQ**: Message broker used for reliable, asynchronous event-driven communication between services.
* **Resilience4j**: Fault tolerance library for implementing circuit breakers and time limiters.
* **Micrometer & OpenTelemetry**: Distributed tracing and observability metrics.
* **Springdoc OpenAPI**: Automated API documentation and Swagger UI generation.

## Architectural Decisions (Crucial Section)

### Synchronous vs. Asynchronous Communication
Our architecture embraces a hybrid communication strategy tailored to the specific needs of each operation:
* **Synchronous REST:** We utilize synchronous HTTP/REST calls specifically for operations requiring immediate consistency and validation, such as verifying a pet's existence in the Customer/Ownership Service before creating a new visit record. This ensures we do not persist orphan data.
* **Asynchronous RabbitMQ Events:** For state propagation across domain boundaries where immediate consistency is not strictly required, we employ asynchronous event publishing via RabbitMQ. This implements Eventual Consistency, reducing temporal coupling, lowering response latency for the end-user, and ensuring high availability even if downstream systems are temporarily unavailable.

### Resilience and Fault Tolerance
To guarantee system stability and prevent resource exhaustion, **Resilience4j** is heavily integrated into inter-service communications (e.g., the synchronous call to validate a pet):
* **Circuit Breaker:** Prevents cascading failures by "tripping" and fast-failing requests when a dependent service (like Customer Service) is down or experiencing high error rates, allowing the failing service time to recover.
* **Time Limiter:** Ensures that synchronous network calls do not block indefinitely, capping wait times and freeing up execution threads if a downstream service becomes unresponsive.

### Error Handling & Standardized Responses
We adhere strictly to **RFC 7807 (Problem Details for HTTP APIs)** to provide standard, machine-readable error responses across all microservices. 
* Business domain errors are carefully distinguished from infrastructure issues.
* **404 Not Found:** Explicitly utilized when requested entities (e.g., Visit or Pet) do not exist, ensuring accurate client-side handling.
* **409 Conflict:** Used when state modifications violate business rules (e.g., conflicting visit schedules), cleanly separating semantic domain errors from generic 400 Bad Request or 500 Internal Server Error responses.

## API Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/visits/{visitId}` | Get a single visit by its ID. Returns 404 if not found. |
| `PUT` | `/api/visits/{visitId}` | Update the description and date of an existing visit. |
| `DELETE` | `/api/visits/{visitId}` | Delete a visit by its ID. |
| `GET` | `/api/visits` | Get all visits with pagination support. |
| `POST` | `/api/visits` | Create a new visit. Validates pet existence before saving. |
| `GET` | `/api/visits/pets/{petId}` | Retrieve all recorded visits associated with a specific pet ID. |

## How to Run

1. **Start Infrastructure Dependencies**
   Ensure your backing services (Eureka Server, RabbitMQ) are running. If using Docker, you can start RabbitMQ via:
   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

2. **Run the Service**
   You can start the Visit Service locally using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Note: The application will start on port `8082` (or a random port depending on Eureka configuration) and connect to an in-memory H2 database.*

3. **Access API Documentation**
   Once running, you can explore the interactive Swagger UI at:
   `http://localhost:8082/swagger-ui.html`

## Testing Approach

Tests use `@WebMvcTest` (controller slice only). `VisitRepository`, `VisitMapper`, and `PetValidationClient` are mocked. Tests are grouped with `@Nested` inner classes per operation (CreateVisit, GetVisitById, etc.).

### Commands

```bash
# Test all
mvn test

# Test a specific class
mvn test -Dtest=VisitControllerTest

# Test a specific method
mvn test -Dtest=VisitControllerTest#shouldCreateVisit
```
