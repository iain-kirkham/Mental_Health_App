package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestSecurityConfiguration;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PomodoroSession API endpoints.
 * Uses Testcontainers for PostgreSQL, disables security for testing,
 * and mocks AuthenticationContext to provide a consistent test user.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class, TestSecurityConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class PomodoroSessionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    // Fixed instant for deterministic tests
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    /**
     * Creates a Pomodoro session directly in the database for test setup purposes.
     * This bypasses the REST API to establish known test data state with realistic
     * Pomodoro session values (25-minute duration, productivity score).
     * Sets the userId to match the test authentication context.
     *
     * @param notesSuffix A unique suffix to append to the notes field for test identification
     * @return The persisted PomodoroSession entity with generated ID and userId
     */
    private PomodoroSession createTestPomodoroSessionInDb(String notesSuffix) {
        PomodoroSession session = new PomodoroSession();
        session.setStartTime(FIXED_NOW);
        session.setEndTime(FIXED_NOW.plusSeconds(25 * 60));
        session.setDuration(25);
        session.setScore((short) 4);
        session.setNotes("Integration test session " + notesSuffix);
        session.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        return pomodoroSessionRepository.save(session);
    }

    // Helper to create a PomodoroSession at a specific start Instant (reduces duplication & flakiness)
    private void createPomodoroAt(Instant startTime, short score, String notes) {
        PomodoroSession s = new PomodoroSession();
        s.setStartTime(startTime);
        s.setEndTime(startTime.plusSeconds(25 * 60));
        s.setDuration(25);
        s.setScore(score);
        s.setNotes(notes);
        s.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        pomodoroSessionRepository.save(s);
    }

    /**
     * Ensures a clean database state before and after each test.
     * This prevents test interference and maintains isolation between test cases.
     */
    @BeforeEach
    @AfterEach
    void cleanUp() {
        pomodoroSessionRepository.deleteAll();
    }

    @Test
    void shouldCreatePomodoroSession() {
        // Arrange: prepare a request DTO instead of entity
        dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO newSession =
            new dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionRequestDTO();
        newSession.setStartTime(FIXED_NOW);
        newSession.setEndTime(FIXED_NOW.plusSeconds(30 * 60));
        newSession.setDuration(30);
        newSession.setScore((short) 5);
        newSession.setNotes("Deep work session.");

        // Act: Send POST request to create Pomodoro session and expect response DTO
        ResponseEntity<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO> response =
            restTemplate.postForEntity(
                "/api/pomodoro",
                newSession,
                dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO.class
        );

        // Assert: Verify successful creation with HTTP 201 and correct response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(created -> {
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getDuration()).isEqualTo(30);
            assertThat(created.getScore()).isEqualTo((short) 5);
            assertThat(created.getNotes()).isEqualTo("Deep work session.");

            // Verify the Pomodoro session was actually persisted to the database
            Optional<PomodoroSession> persistedEntity = pomodoroSessionRepository.findById(created.getId());
            assertThat(persistedEntity).isPresent();
            assertThat(persistedEntity.get().getDuration()).isEqualTo(30);
            assertThat(persistedEntity.get().getNotes()).isEqualTo("Deep work session.");
            assertThat(persistedEntity.get().getUserId()).isEqualTo(TestAuthenticationConfig.TEST_USER_ID);
        });
    }

    @Test
    void shouldGetAllPomodoroSessions() {
        // Arrange: Create test data - two Pomodoro sessions with different identifiers
        createTestPomodoroSessionInDb("1");
        createTestPomodoroSessionInDb("2");

        // Act: Send GET request to retrieve all Pomodoro sessions and expect response DTOs
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: Verify successful retrieval with correct count and data structure
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Concise chained assertions using AssertJ on the response body
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .first()
                .satisfies(s -> {
                    assertThat(s.getId()).isNotNull();
                    assertThat(s.getDuration()).isEqualTo(25);
                    assertThat(s.getNotes()).contains("Integration test session");
                });
    }

    @Test
    void shouldGetPomodoroSessionById() {
        // Arrange: Create a specific Pomodoro session to retrieve by ID
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("for lookup");

        // Act: Send GET request for specific Pomodoro session by ID
        ResponseEntity<PomodoroSession> response = restTemplate.getForEntity(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                PomodoroSession.class
        );

        // Assert: Verify successful retrieval with matching data from database entity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(existingSessionEntity.getId());
            assertThat(body.getDuration()).isEqualTo(existingSessionEntity.getDuration());
            assertThat(body.getNotes()).isEqualTo(existingSessionEntity.getNotes());
        });
    }

    @Test
    void shouldReturnNotFoundForNonExistentPomodoroSession() {
        // Act: Attempt to retrieve Pomodoro session using non-existent ID
        ResponseEntity<PomodoroSession> response = restTemplate.getForEntity(
                "/api/pomodoro/999",
                PomodoroSession.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdatePomodoroSession() {
        // Arrange: Create existing Pomodoro session in database to update
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("to be updated");

        // Arrange: Prepare update data with modified values (extended 60-minute session)
        PomodoroSession updateSession = new PomodoroSession();
        updateSession.setStartTime(FIXED_NOW.minusSeconds(120));
        updateSession.setEndTime(FIXED_NOW.plusSeconds(60 * 60)); // 60 minutes
        updateSession.setDuration(60);
        updateSession.setScore((short) 5);
        updateSession.setNotes("Updated a super focused session!");

        // Act: Send PUT request to update existing Pomodoro session
        ResponseEntity<PomodoroSession> response = restTemplate.exchange(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateSession),
                PomodoroSession.class
        );

        // Assert: Verify successful update with HTTP 200 and updated response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(existingSessionEntity.getId());
            assertThat(body.getDuration()).isEqualTo(60);
            assertThat(body.getScore()).isEqualTo((short) 5);
            assertThat(body.getNotes()).isEqualTo("Updated a super focused session!");

            // Assert: Verify the changes were persisted to the database
            PomodoroSession fetchedFromDb = pomodoroSessionRepository.findById(existingSessionEntity.getId()).orElse(null);
            assertThat(fetchedFromDb).isNotNull();
            assertThat(fetchedFromDb.getDuration()).isEqualTo(60);
            assertThat(fetchedFromDb.getNotes()).isEqualTo("Updated a super focused session!");
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPomodoroSession() {
        // Arrange: Prepare update data for non-existent Pomodoro session
        PomodoroSession updateSession = new PomodoroSession();
        updateSession.setStartTime(FIXED_NOW);
        updateSession.setEndTime(FIXED_NOW.plusSeconds(25 * 60));
        updateSession.setDuration(25);
        updateSession.setScore((short) 4);
        updateSession.setNotes("Non-existent update");

        // Act: Attempt to update Pomodoro session that doesn't exist
        ResponseEntity<PomodoroSession> response = restTemplate.exchange(
                "/api/pomodoro/999",
                HttpMethod.PUT,
                new HttpEntity<>(updateSession),
                PomodoroSession.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeletePomodoroSession() {
        // Arrange: Create Pomodoro session in database to be deleted
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("to be deleted");

        // Act: Send DELETE request to remove the Pomodoro session
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                HttpMethod.DELETE,
                null, // No request body required for DELETE operation
                Void.class // No response body expected for successful deletion
        );

        // Assert: Verify successful deletion with HTTP 204 No Content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Assert: Verify the Pomodoro session was actually removed from the database
        assertThat(pomodoroSessionRepository.findById(existingSessionEntity.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPomodoroSession() {
        // Act: Attempt to delete Pomodoro session that doesn't exist
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/pomodoro/999", // ID that doesn't exist in database
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- New date-range integration tests ---

    @Test
    void getPomodoroSessionsByDateRange_ShouldReturnOnlySessionsInRange() {
        // Arrange: create three sessions at different times
        Instant now = FIXED_NOW;
        createPomodoroAt(now.minus(10, ChronoUnit.DAYS), (short)2, "older");
        createPomodoroAt(now.minus(5, ChronoUnit.DAYS), (short)3, "middle");
        createPomodoroAt(now, (short)4, "recent");

        // Act: query for sessions from 7 days ago to now -> should return middle and recent
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        String end = now.plus(1, ChronoUnit.DAYS).toString();

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .extracting(dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO::getNotes)
                .containsExactly("recent", "middle");
    }

    @Test
    void getPomodoroSessionsByDateRange_ShouldReturnNoContentWhenNoMatches() {
        // Arrange: create a session outside the queried window
        Instant now = FIXED_NOW;
        createPomodoroAt(now.minus(30, ChronoUnit.DAYS), (short)3, "out-of-range");

        // Act: query recent dates where there are no sessions
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        String end = now.toString();

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: controller returns 204 No Content when list empty
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getPomodoroSessionsByDateRange_InvalidDateFormat_ReturnsBadRequest() {
        // Act: call endpoint with invalid date format
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/pomodoro?startDate=not-a-date&endDate=also-not-a-date",
                HttpMethod.GET,
                null,
                String.class
        );

        // Assert: Spring should return 400 Bad Request for unparsable Instants
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- Partial-date and boundary tests ---

    @Test
    void getPomodoroSessionsByDateRange_PartialStartDate_ReturnsAllSessions() {
        // Arrange: create three sessions at different times
        Instant now = FIXED_NOW;
        createPomodoroAt(now.minus(10, ChronoUnit.DAYS), (short)2, "older");
        createPomodoroAt(now.minus(5, ChronoUnit.DAYS), (short)3, "middle");
        createPomodoroAt(now, (short)4, "recent");

        // Act: call with only startDate
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro?startDate=" + start,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: current controller behavior: partial params ignored -> return all
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(3);
    }

    @Test
    void getPomodoroSessionsByDateRange_PartialEndDate_ReturnsAllSessions() {
        // Arrange: create simple sessions
        createTestPomodoroSessionInDb("A");
        createTestPomodoroSessionInDb("B");
        createTestPomodoroSessionInDb("C");

        // Act: call with only endDate
        String end = FIXED_NOW.toString();
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro?endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: controller currently treats partial params as no-op -> returns all
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(3);
    }

    @Test
    void getPomodoroSessionsByDateRange_BoundaryInclusivity_StartAndEndInclusive() {
        // Arrange: set explicit sessions at the start and end boundaries
        Instant now = FIXED_NOW;
        Instant startInstant = now.minus(7, ChronoUnit.DAYS);
        createPomodoroAt(startInstant, (short)1, "start");
        createPomodoroAt(now, (short)5, "end");

        // Act: query inclusive window
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO>> response =
            restTemplate.exchange(
                "/api/pomodoro?startDate=" + startInstant + "&endDate=" + now,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: results should include both boundary entries; ordering is newest-first
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO::getNotes)
                .containsExactly("end", "start");
    }

}
