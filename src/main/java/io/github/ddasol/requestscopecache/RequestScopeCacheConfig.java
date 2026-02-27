package io.github.ddasol.requestscopecache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RequestScopeCacheConfig {

    @Bean(name = "requestScopeCacheManager")
    public CacheManager requestScopeCacheManager() { return new RequestScopeCacheManager(); }
}
