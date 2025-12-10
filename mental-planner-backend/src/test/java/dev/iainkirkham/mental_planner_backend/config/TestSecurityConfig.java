package dev.iainkirkham.mental_planner_backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test security configuration that disables JWT authentication for integration tests.
 * This allows tests to call API endpoints without needing valid JWT tokens.
 *
 * By mocking the JwtDecoder, Spring Security's OAuth2 resource server
 * configuration won't try to validate JWT tokens during tests.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Mock JwtDecoder to prevent actual JWT validation during tests.
     * The test AuthenticationContext will provide the necessary user context.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;
}

