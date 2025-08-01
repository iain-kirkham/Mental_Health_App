package dev.iainkirkham.mental_planner_backend.pomodoro;

import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionCreationDTO;
import dev.iainkirkham.mental_planner_backend.pomodoro.dto.PomodoroSessionResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class PomodoroSessionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    /**
     * Creates a Pomodoro session directly in the database for test setup purposes.
     * This bypasses the REST API to establish known test data state with realistic
     * Pomodoro session values (25-minute duration, productivity score).
     *
     * @param notesSuffix A unique suffix to append to the notes field for test identification
     * @return The persisted PomodoroSession entity with generated ID
     */
    private PomodoroSession createTestPomodoroSessionInDb(String notesSuffix) {
        PomodoroSession session = new PomodoroSession();
        session.setStartTime(Instant.now());
        session.setEndTime(Instant.now().plusSeconds(25 * 60));
        session.setDuration(25);
        session.setScore((short) 4);
        session.setNotes("Integration test session " + notesSuffix);
        return pomodoroSessionRepository.save(session);
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
        // Arrange: Prepare a new Pomodoro session creation request with a 30-minute duration
        PomodoroSessionCreationDTO newSessionDTO = new PomodoroSessionCreationDTO();
        newSessionDTO.setStartTime(Instant.now());
        newSessionDTO.setEndTime(Instant.now().plusSeconds(30 * 60));
        newSessionDTO.setDuration(30);
        newSessionDTO.setScore((short) 5);
        newSessionDTO.setNotes("Deep work session.");

        // Act: Send POST request to create Pomodoro session and expect response DTO
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.postForEntity(
                "/api/pomodoro", // REST endpoint for Pomodoro session management
                newSessionDTO, // Request body: CreationDTO with session details
                PomodoroSessionResponseDTO.class // Expected response type: ResponseDTO
        );

        // Assert: Verify successful creation with HTTP 201 and correct response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getDuration()).isEqualTo(30);
        assertThat(response.getBody().getScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getNotes()).isEqualTo("Deep work session.");

        // Assert: Verify the Pomodoro session was actually persisted to the database
        Optional<PomodoroSession> persistedEntity = pomodoroSessionRepository.findById(response.getBody().getId());
        assertThat(persistedEntity).isPresent();
        assertThat(persistedEntity.get().getDuration()).isEqualTo(30);
        assertThat(persistedEntity.get().getNotes()).isEqualTo("Deep work session.");
    }

    @Test
    void shouldGetAllPomodoroSessions() {
        // Arrange: Create test data - two Pomodoro sessions with different identifiers
        createTestPomodoroSessionInDb("1");
        createTestPomodoroSessionInDb("2");

        // Act: Send GET request to retrieve all Pomodoro sessions as response DTOs
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = restTemplate.exchange(
                "/api/pomodoro",
                HttpMethod.GET,
                null, // No request body required for GET operation
                new ParameterizedTypeReference<List<PomodoroSessionResponseDTO>>() {} // Use ResponseDTO
        );

        // Assert: Verify successful retrieval with correct count and data structure
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        // Assert: Verify the returned items are properly formatted ResponseDTOs with expected content
        assertThat(response.getBody().get(0).getId()).isNotNull();
        assertThat(response.getBody().get(0).getDuration()).isEqualTo(25);
        assertThat(response.getBody().get(0).getNotes()).contains("Integration test session");
    }

    @Test
    void shouldGetPomodoroSessionById() {
        // Arrange: Create a specific Pomodoro session to retrieve by ID
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("for lookup");

        // Act: Send GET request for specific Pomodoro session by ID, expecting response DTO
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.getForEntity(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                PomodoroSessionResponseDTO.class // Expecting ResponseDTO
        );

        // Assert: Verify successful retrieval with matching data from database entity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingSessionEntity.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(existingSessionEntity.getDuration());
        assertThat(response.getBody().getNotes()).isEqualTo(existingSessionEntity.getNotes());
    }

    @Test
    void shouldReturnNotFoundForNonExistentPomodoroSession() {
        // Act: Attempt to retrieve Pomodoro session using non-existent ID
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.getForEntity(
                "/api/pomodoro/999", // ID that doesn't exist in database
                PomodoroSessionResponseDTO.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdatePomodoroSession() {
        // Arrange: Create existing Pomodoro session in database to update
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("to be updated");

        // Arrange: Prepare update data with modified values (extended 60-minute session)
        PomodoroSessionCreationDTO updateDTO = new PomodoroSessionCreationDTO();
        updateDTO.setStartTime(Instant.now().minusSeconds(120));
        updateDTO.setEndTime(Instant.now().plusSeconds(60 * 60)); // 60 minutes
        updateDTO.setDuration(60);
        updateDTO.setScore((short) 5);
        updateDTO.setNotes("Updated a super focused session!");

        // Act: Send PUT request to update existing Pomodoro session
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.exchange(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO), // Request body: CreationDTO with updated values
                PomodoroSessionResponseDTO.class // Expected response type: ResponseDTO
        );

        // Assert: Verify successful update with HTTP 200 and updated response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingSessionEntity.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(60);
        assertThat(response.getBody().getScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getNotes()).isEqualTo("Updated a super focused session!");

        // Assert: Verify the changes were persisted to the database
        PomodoroSession fetchedFromDb = pomodoroSessionRepository.findById(existingSessionEntity.getId()).orElse(null);
        assertThat(fetchedFromDb).isNotNull();
        assertThat(fetchedFromDb.getDuration()).isEqualTo(60);
        assertThat(fetchedFromDb.getNotes()).isEqualTo("Updated a super focused session!");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPomodoroSession() {
        // Arrange: Prepare update data for non-existent Pomodoro session
        PomodoroSessionCreationDTO updateDTO = new PomodoroSessionCreationDTO();
        updateDTO.setStartTime(Instant.now());
        updateDTO.setEndTime(Instant.now().plusSeconds(25 * 60));
        updateDTO.setDuration(25);
        updateDTO.setScore((short) 4);
        updateDTO.setNotes("Non-existent update");

        // Act: Attempt to update Pomodoro session that doesn't exist
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.exchange(
                "/api/pomodoro/999",  // ID that doesn't exist in database
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO),
                PomodoroSessionResponseDTO.class
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
}