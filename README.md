# Smart Campus API

## 📚 API Documentation

### Overview
The Smart Campus API is a RESTful service designed to manage university rooms and the environmental sensors deployed within them. It provides endpoints for creating, retrieving, and deleting rooms; registering and filtering sensors; and recording historical sensor readings. The API follows REST architectural principles, uses JSON for data exchange, and implements robust error handling with appropriate HTTP status codes.

**Base URL:** `http://localhost:8080/SmartCampusAPI/api/v1`

### Build and Run Instructions

#### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Apache Maven 3.6+
- Apache Tomcat 9.x
- NetBeans IDE 

#### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Punsara-Rajapaksa/smart-campus-api.git
   ```

2. **Open the project in NetBeans:**
   - Select **File → Open Project**.
   - Navigate to the cloned folder and click **Open Project**.

3. **Add Apache Tomcat Server** (if not already configured):
   - In the **Services** tab, right‑click **Servers → Add Server**.
   - Select **Apache Tomcat** and locate your Tomcat installation directory.
   - Enter a username and password (e.g., `admin` / `admin`) and click **Finish**.

4. **Build the project:**
   - Right‑click the project name in the **Projects** tab.
   - Select **Clean and Build**.

5. **Run the application:**
   - Right‑click the project and select **Run**.
   - Tomcat will start and deploy the API at `http://localhost:8080/SmartCampusAPI`.

6. **Verify deployment:**
   - Open a browser and navigate to `http://localhost:8080/SmartCampusAPI/api/v1`.
   - You should see the Discovery endpoint JSON response containing API version and resource links.

### Sample cURL Commands

#### 1. Discovery Endpoint – Get API metadata
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

#### 2. Get All Rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

#### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms -H "Content-Type: application/json" -d "{\"name\":\"Conference Room\",\"location\":\"Building D\",\"capacity\":25}"
```

#### 4. Get Sensors Filtered by Type (e.g., CO2)
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

#### 5. Add a Sensor Reading (updates sensor's current value)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/1/readings -H "Content-Type: application/json" -d "{\"value\":465.5}"
```

---

## Coursework Report

## Part 1: Service Architecture & Setup

### 1.1 JAX‑RS Resource Lifecycle & In‑Memory Data Synchronisation

By default, JAX‑RS resource classes are request‑scoped: the runtime creates a new instance of the resource class for every incoming HTTP request. This means instance variables are not shared across requests, which is safe but may lead to higher object allocation overhead.

For our in‑memory data structures (e.g., `HashMap`, `ArrayList`), they must be declared as `static` fields or stored in a separate singleton DAO class so that all resource instances access the same shared data. Without proper synchronisation (e.g., using `ConcurrentHashMap` or `synchronized` blocks), concurrent requests could cause race conditions or data corruption.

The `@Singleton` annotation can be used to make a resource class application‑scoped (one instance for the whole app), but then all mutable shared state must be thread‑safe.

### 1.2 HATEOAS Benefits

HATEOAS is a REST constraint where API responses include hypermedia links that guide clients on possible next actions. In our Discovery endpoint, we provided a `resources` map with links to `/rooms` and `/sensors`.

**Benefits over static documentation:**
- **Discoverability:** Clients can navigate the API dynamically without hardcoding URLs.
- **Loose Coupling:** The server can change URI structures without breaking clients, as long as link relations remain consistent.
- **Self‑Documentation:** The API becomes explorable, reducing reliance on out‑of‑date external docs.
- **State Transitions:** Clients understand valid state transitions (e.g., from a room to its sensors) by following links.

## Part 2: Room Management

### 2.1 Returning IDs vs Full Objects

When returning a list of rooms, returning only IDs reduces network bandwidth significantly because the response contains just integer values rather than complete JSON objects. However, this forces the client to make an additional HTTP request for each room to retrieve full details, increasing latency and client‑side processing complexity.

Returning full room objects in a single request eliminates extra round trips and simplifies client logic, but the payload size grows with the number of rooms and fields. For a small‑scale campus API with modest data, the convenience of full objects outweighs the bandwidth concern. 

### 2.2 Idempotency of DELETE

The `DELETE` operation in our implementation is idempotent. After a successful deletion (HTTP `204`), the room no longer exists. If the client mistakenly sends the same `DELETE` request again, the server cannot find the room and returns `404 Not Found`. The server state remains unchanged (the room is still absent).

Even if the client sends the request 100 times, the outcome is identical: the room stays deleted. This satisfies the idempotency constraint because the effect of multiple identical requests is the same as a single request.

## Part 3: Sensor Operations & Linking

### 3.1 @Consumes and Media Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX‑RS that the `POST` method only accepts requests with a `Content-Type: application/json` header. If a client sends data in a different format (e.g., `text/plain` or `application/xml`), JAX‑RS cannot find a suitable `MessageBodyReader` to deserialize the request body. The runtime then returns an HTTP 415 Unsupported Media Type error, indicating the server refuses to process the payload because its format is not supported.

### 3.2 @QueryParam vs Path Parameter for Filtering

We implemented filtering using `@QueryParam("type")`, resulting in URLs like `/sensors?type=CO2`. An alternative design would embed the type in the path: `/sensors/type/CO2`.

The query parameter approach is superior for filtering collections because:
- **Semantics:** Query parameters are designed for optional, non‑hierarchical modifiers. The path should identify a specific resource or sub‑collection.
- **Flexibility:** Multiple independent filters can be combined easily (`?type=CO2&status=ACTIVE`). With path parameters, combining filters becomes messy or impossible.
- **Discoverability:** The base resource URL (`/sensors`) remains clean and consistent regardless of which filters are applied.
- **RESTful Conventions:** Leading APIs (e.g., Google, GitHub) use query parameters for optional filtering, following established best practices.

## Part 4: Deep Nesting with Sub‑Resources

### 4.1 Benefits of the Sub‑Resource Locator Pattern

The sub‑resource locator pattern delegates a sub‑path (`{sensorId}/readings`) to a separate resource class (`SensorReadingResource`). This offers several architectural advantages over defining all nested paths in a single monolithic controller:

- **Separation of Concerns:** Each resource class handles a single responsibility. `SensorResource` manages sensor metadata; `SensorReadingResource` manages historical readings.
- **Code Maintainability:** Smaller, focused classes are easier to read, test, and debug.
- **Reusability:** The sub‑resource class can be instantiated with different parent contexts (e.g., also used under `/rooms/{roomId}/sensors/{sensorId}/readings` if needed).
- **Natural Hierarchy:** The URL structure (`/sensors/1/readings`) maps cleanly to object‑oriented design, making the API intuitive.

### 4.2 Updating Parent Sensor on POST

When a new reading is posted to `/sensors/{sensorId}/readings`, the API updates the parent sensor's `currentValue` field. This ensures data consistency across the API: the `currentValue` always reflects the most recent reading without requiring clients to make an additional request. It also mirrors real‑world sensor behaviour where the latest reading is a summary attribute of the sensor itself.

### 5.1 Why 422 over 404 for Missing Reference in Payload?

HTTP `404 Not Found` indicates that the requested URI does not exist. In the case of a `POST /sensors` with a valid URI but an invalid `roomId` in the JSON body, the URI itself is correct, the resource being operated on (`/sensors`) exists. The problem is with the semantic validity of the provided data. HTTP `422 Unprocessable Entity` signals that the server understands the content type and syntax, but cannot process the contained instructions due to a logical error. This is more accurate and provides clearer client guidance.

### 5.2 Cybersecurity Risks of Exposing Stack Traces

Exposing Java stack traces to external API consumers reveals sensitive internal information, including:
- **Package and class names**: Giving attackers insight into the application's internal architecture.
- **Library versions**: Potentially exposing known vulnerabilities in specific versions of third‑party libraries.
- **File paths and line numbers**: Disclosing the server's directory structure.
- **Database schema details**: A `SQLException` may leak table names, column names, or query structure.

An attacker can use this information to craft targeted exploits. The global `ExceptionMapper<Throwable>` safely logs the full trace server‑side while returning only a generic error to the client.

### 5.3 Advantages of JAX‑RS Filters for Logging

Using `ContainerRequestFilter` and `ContainerResponseFilter` for cross‑cutting concerns like logging is superior to manually inserting `Logger.info()` statements in every resource method because:
- **Single Responsibility:** Filters centralise logging logic, keeping resource classes focused on business rules.
- **Consistency:** Guarantees every request/response is logged uniformly, without relying on developers to remember.
- **Maintainability:** Changes to logging format or level are made in one place.
- **Non‑invasive:** Filters do not alter the core logic of resource methods, preserving readability.