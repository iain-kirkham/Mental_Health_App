package dev.iainkirkham.mental_planner_backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides a mock AuthenticationContext for integration tests.
 * This mock always returns a consistent test user ID, allowing tests to run without
 * requiring actual JWT authentication.
 */
@TestConfiguration
public class TestAuthenticationConfig {

    public static final String TEST_USER_ID = "user_integration_test_123";

    @Bean
    @Primary
    public AuthenticationContext testAuthenticationContext() {
        return new AuthenticationContext() {
            @Override
            public String getCurrentUserId() {
                return TEST_USER_ID;
            }

            @Override
            public Object getClaim(String claimName) {
                if ("email".equals(claimName)) {
                    return "test@example.com";
                }
                return null;
            }

            @Override
            public String getUserEmail() {
                return "test@example.com";
            }
        };
    }
}
