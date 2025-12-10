package dev.iainkirkham.mental_planner_backend.config;

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
     * @throws ClassCastException if no JWT is present in the security context
     */
    public String getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        // Clerk stores the user ID in the "sub" claim
        return jwt.getSubject();
    }

    /**
     * Gets a specific claim from the JWT.
     *
     * @param claimName the name of the claim to retrieve
     * @return the claim value, or null if the claim doesn't exist
     * @throws ClassCastException if no JWT is present in the security context
     */
    public Object getClaim(String claimName) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        return jwt.getClaim(claimName);
    }

    /**
     * Gets the user's email from the JWT.
     *
     * @return the user's email address
     * @throws ClassCastException if no JWT is present in the security context
     */
    public String getUserEmail() {
        return (String) getClaim("email");
    }
}

