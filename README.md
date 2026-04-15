# Smart Campus API – Coursework Report

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