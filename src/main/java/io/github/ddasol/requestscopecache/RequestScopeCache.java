package io.github.ddasol.requestscopecache;

import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public class RequestScopeCache implements Cache {

    private final String name;
    private static final String ATTR_PREFIX = "request-cache-";

    public RequestScopeCache(String name) { this.name = name; }

    @Override
    public String getName() { return name; }

    @Override
    public Object getNativeCache() { return getStore(true); }

    @Nullable
    @Override
    public ValueWrapper get(Object key) {
        Object v = getStore(false).get(key);
        return v != null ? () -> v : null;
    }

    @Nullable
    @Override
    public <T> T get(Object key, @Nullable Class<T> type) {
        Object v = getStore(false).get(key);
        if (v == null) return null;
        return (type == null || type.isInstance(v)) ? (T) v : null;
    }

    @Nullable
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Map<Object, Object> store = getStore(true);
        Object existing = store.get(key);
        if (existing != null) {
            // hit
            @SuppressWarnings("unchecked")
            T cast = (T) existing;
            return cast;
        }

        // miss -> load, store and return
        try {
            T value = valueLoader.call();
            store.put(key, value);
            return value;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        getStore(true).put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Map<Object, Object> store = getStore(true);
        Object prev = store.putIfAbsent(key, value);
        return (prev != null) ? () -> prev : null;
    }

    @Override
    public void evict(Object key) {
        getStore(false).remove(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return getStore(false).remove(key) != null;
    }

    @Override
    public void clear() {
        getStore(false).clear();
    }

    private Map<Object, Object> getStore(boolean createIfAbsent) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            // outside request thread: returns empty map
            return new HashMap<>();
        }
        String attrName = ATTR_PREFIX + name;
        Object existing = attrs.getAttribute(attrName, RequestAttributes.SCOPE_REQUEST);
        if (existing == null && createIfAbsent) {
            existing = new HashMap<>();
            attrs.setAttribute(attrName, existing, RequestAttributes.SCOPE_REQUEST);
        }
        return (Map<Object, Object>) (existing != null ? existing : new HashMap<>());
    }
}
