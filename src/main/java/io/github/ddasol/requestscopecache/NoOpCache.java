package io.github.ddasol.requestscopecache;

import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

public class NoOpCache implements Cache {

    private final String name;

    public NoOpCache(String name) { this.name = name; }

    @Override
    public String getName() { return name; }

    @Override
    public Object getNativeCache() { return this; }

    @Nullable
    @Override
    public ValueWrapper get(Object key) { return null; }

    @Nullable
    @Override
    public <T> T get(Object key, @Nullable Class<T> type) { return null; }

    @Nullable
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            // bypass: only executes loader
            return valueLoader.call();
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) { /* no-op */ }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        // always empty: it returns null
        return null;
    }

    @Override
    public void evict(Object key) { /* no-op */ }

    @Override
    public boolean evictIfPresent(Object key) {
        // nothing to remove
        return false;
    }

    @Override
    public void clear() { /* no-op */ }
}
