package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.crypto.SecretKey;

/**
 * Holds the current user's unwrapped data encryption key for the duration of one HTTP
 * request, so an entity with several encrypted fields only unwraps its owner's DEK once
 * rather than once per field. Discarded (and its contents garbage-collected) when the
 * request completes - nothing here is ever persisted or logged.
 */
@Component
@RequestScope
public class CurrentUserKeyCache {

    private String userId;
    private SecretKey dataKey;

    public SecretKey get(String userId) {
        return userId.equals(this.userId) ? dataKey : null;
    }

    public void put(String userId, SecretKey dataKey) {
        this.userId = userId;
        this.dataKey = dataKey;
    }
}
