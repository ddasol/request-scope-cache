
# Request Scope CacheManager (per-request @Cacheable with automatic bypass)

This module provides a `CacheManager` and cache implementations that enable **per-request caching** when using Spring’s `@Cacheable`, while automatically **bypassing the cache** when execution occurs **outside the request thread** (e.g., `@Async`, `parallelStream`, custom executors, or thread pools).

It is designed for microservice architectures where repeated calls to internal configuration services need to be minimized **within a single request**, but **should not leak across requests** or **interfere with asynchronous processing**.

---

## Features

### Per-request caching
- Cache entries live **only during the lifetime of the HTTP request**.
- Multiple calls to a `@Cacheable` method with the same key inside the same request will result in **only one remote call**.

### Safe automatic bypass
- When invoked outside the request thread, the cache becomes a **No-Op Cache**, meaning:
  - The method executes normally.
  - No data is cached.
  - No exceptions occur.
  - Ideal for async tasks, parallel streams, and thread pools.

### Plug‑and‑play integration
- Add the module to your project.
- Register `RequestScopeCacheManager` as a bean.
- Use `@Cacheable(cacheManager = "requestScopeCacheManager")`.

### No global caching
- The cache is intentionally **ephemeral** and **per-request**, not shared across threads or requests.
- Perfect for reducing redundant internal service calls without global side effects.

---

## Components

### `RequestScopeCacheManager`
Determines dynamically whether to return:
- `RequestScopeCache` (if the current thread belongs to an active HTTP request), or
- `NoOpCache` (if there is **no request context**, e.g., async execution).

### `RequestScopeCache`
- Stores cache entries in Spring’s `RequestAttributes`.
- Keys and values persist only until the request completes.

### `NoOpCache`
- A safe “bypass” cache.
- Executes `Callable` loaders normally but **never stores values**.
- Ensures async scenarios do not break when `@Cacheable` is used.

---

## How to use

### 1. Enable caching and register the CacheManager
```java
@Configuration
@EnableCaching
public class RequestScopeCacheConfig {

    @Bean(name = "requestScopeCacheManager")
    public CacheManager requestScopeCacheManager() {
        return new RequestScopeCacheManager();
    }
}
```

### 2. Annotate your methods
```java
@Cacheable(value = "params", key = "#keyName", cacheManager = "requestScopeCacheManager")
public ParamsConfig getParameters(String keyName) { ... }
```

### 3. Behavior summary
| Context                           | Cache behavior         |
|----------------------------------|-------------------------|
| Inside request thread            | Uses per-request cache |
| Outside request (@Async, etc.)   | NoOp (bypass) cache    |

---

## Notes

- This module does **not** interfere with any global cache (Redis, Caffeine, etc.).
- If you use multiple `CacheManager`s, always specify `cacheManager = "requestScopeCacheManager"` in your `@Cacheable` annotation.
- Per-request caching is ideal for reducing repeated calls to internal microservices.

---

## Requirements
- Java 17+
- Spring Boot 3.x (or Spring Framework 6+)

---