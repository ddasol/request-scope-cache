package io.github.ddasol.requestscopecache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class RequestScopeCacheManager implements CacheManager {

    private final ConcurrentHashMap<String, Cache> cachesWithRequest = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Cache> cachesNoRequest = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public Cache getCache(String name) {
        boolean hasRequest = RequestContextHolder.getRequestAttributes() != null;
        if (hasRequest) {
            return cachesWithRequest.computeIfAbsent(name, RequestScopeCache::new);
        } else {
            return cachesNoRequest.computeIfAbsent(name, NoOpCache::new);
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        var union = new java.util.HashSet<String>();
        union.addAll(cachesWithRequest.keySet());
        union.addAll(cachesNoRequest.keySet());
        return Collections.unmodifiableSet(union);
    }
}
