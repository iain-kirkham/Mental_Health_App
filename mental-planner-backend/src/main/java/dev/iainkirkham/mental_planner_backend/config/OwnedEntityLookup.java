package dev.iainkirkham.mental_planner_backend.config;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Shared seam for the "find an entity scoped to the current user, or 404" idiom
 * repeated across every feature service (tasks, pomodoro sessions, mood entries).
 * <p>
 * This is for a service verifying ownership of <i>its own</i> entity. A service that only
 * references another domain's entity by ID (e.g. pomodoro linking to a Task) doesn't use this
 * directly - it goes through that owning service's own ownership check instead (see e.g.
 * {@code TaskService.assertOwnedByCurrentUser}), since only the owning service's repository
 * knows the real query and any business rules layered onto it later.
 */
@Component
public class OwnedEntityLookup {

    private final AuthenticationContext authenticationContext;

    public OwnedEntityLookup(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Looks up an entity by ID, scoped to the current user, or throws.
     *
     * @param finder the repository's findByIdAndUserId method reference
     * @param id the entity's ID
     * @param entityName the entity's display name, used in the not-found message (e.g. "Task")
     * @throws ResourceNotFoundException if no matching entity is found for the current user
     */
    public <T> T findOwnedOrThrow(BiFunction<Long, String, Optional<T>> finder, Long id, String entityName) {
        String userId = authenticationContext.getCurrentUserId();
        return finder.apply(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + " not found with ID: " + id));
    }
}
