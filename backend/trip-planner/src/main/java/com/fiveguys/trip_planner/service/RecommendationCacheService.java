package com.fiveguys.trip_planner.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecommendationCacheService {

    private static class CacheEntry {
        private final Object value;
        private final long expiresAtMillis;

        public CacheEntry(Object value, long expiresAtMillis) {
            this.value = value;
            this.expiresAtMillis = expiresAtMillis;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        return (T) entry.getValue();
    }

    public void put(String key, Object value, Duration ttl) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}