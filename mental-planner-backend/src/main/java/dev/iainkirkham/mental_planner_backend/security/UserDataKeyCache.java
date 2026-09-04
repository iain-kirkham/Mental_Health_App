package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, bounded in-memory cache of unwrapped per-user data encryption keys, so an
 * entity with several encrypted fields (or several saves/loads in a row) only unwraps a
 * user's DEK once per TTL window rather than once per field access.
 * <p>
 * Deliberately not request-scoped: encryption/decryption can legitimately happen outside
 * an HTTP request (background jobs, admin tooling, tests calling a repository directly),
 * and a request-scoped cache throws in all of those. A short TTL keeps the exposure window
 * small without depending on the servlet request lifecycle. Nothing here is ever logged or
 * persisted, and entries are cleared on expiry so nothing lingers indefinitely.
 */
@Component
public class UserDataKeyCache {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, CachedKey> cache = new ConcurrentHashMap<>();

    public SecretKey get(String userId) {
        CachedKey cached = cache.get(userId);
        if (cached == null) {
            return null;
        }
        if (cached.isExpired()) {
            cache.remove(userId, cached);
            return null;
        }
        return cached.key();
    }

    public void put(String userId, SecretKey dataKey) {
        cache.put(userId, new CachedKey(dataKey, Instant.now().plus(TTL)));
    }

    private record CachedKey(SecretKey key, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
