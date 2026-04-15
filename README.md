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