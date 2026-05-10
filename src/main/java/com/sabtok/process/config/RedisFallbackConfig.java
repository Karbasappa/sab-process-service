package com.sabtok.process.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisFallbackConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        // This is the key: it catches Redis errors and allows
        // the execution to fall through to your @Cacheable method (the DB).
        return new LoggingCacheErrorHandler();
    }
}
