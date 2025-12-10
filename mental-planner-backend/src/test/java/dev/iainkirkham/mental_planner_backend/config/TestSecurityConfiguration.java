package dev.iainkirkham.mental_planner_backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

/**
 * Test-specific security configuration that completely disables authentication.
 * This is activated only when the 'test' profile is active, allowing integration
 * tests to call API endpoints without JWT tokens.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfiguration {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * Filter that injects a Jwt principal into the security context for tests.
     * The Jwt contains a 'sub' claim equal to the test user id so the real
     * AuthenticationContext can extract the user id as it would in production.
     */
    @Bean
    public OncePerRequestFilter testAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                try {
                    Jwt jwt = Jwt.withTokenValue("test-token")
                            .subject(TestAuthenticationConfig.TEST_USER_ID)
                            .claim("email", "test@example.com")
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(3600))
                            .header("alg", "none")
                            .build();

                    Authentication auth = new TestingAuthenticationToken(jwt, null, "ROLE_USER");
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    // If Jwt builder not available, skip; tests may still pass via TestAuthenticationConfig bean
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
