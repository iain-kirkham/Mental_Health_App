package dev.iainkirkham.mental_planner_backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility component for accessing the current authenticated user's information from the JWT token.
 * This class extracts user details from the Clerk JWT that has been validated by Spring Security.
 */
@Component
public class AuthenticationContext {

    /**
     * Gets the current authenticated user's Clerk user ID.
     * The user ID is extracted from the "sub" (subject) claim of the JWT.
     *
     * @return the Clerk user ID (e.g., "user_2abc123def456")
     * @throws IllegalStateException if no JWT authentication is present in the security context
     */
    public String getCurrentUserId() {
        return getJwt().getSubject();
    }

    /**
     * Gets a specific claim from the JWT.
     *
     * @param claimName the name of the claim to retrieve
     * @return the claim value, or null if the claim doesn't exist
     * @throws IllegalStateException if no JWT authentication is present in the security context
     */
    public Object getClaim(String claimName) {
        return getJwt().getClaim(claimName);
    }

    /**
     * Gets the user's email from the JWT.
     *
     * @return the user's email address
     * @throws IllegalStateException if no JWT authentication is present in the security context
     */
    public String getUserEmail() {
        return (String) getClaim("email");
    }

    /**
     * Extracts the JWT from the current security context with proper null safety.
     *
     * @return the JWT principal
     * @throws IllegalStateException if the security context has no JWT authentication
     */
    private Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No JWT authentication present in security context");
        }
        return jwt;
    }
}

