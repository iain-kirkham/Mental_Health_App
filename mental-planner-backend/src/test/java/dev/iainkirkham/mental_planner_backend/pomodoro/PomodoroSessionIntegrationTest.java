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

    // Helper method to create a PomodoroSession in the DB directly for test setup
    private PomodoroSession createTestPomodoroSessionInDb(String notesSuffix) {
        PomodoroSession session = new PomodoroSession();
        session.setStartTime(Instant.now());
        session.setEndTime(Instant.now().plusSeconds(25 * 60));
        session.setDuration(25);
        session.setScore((short) 4);
        session.setNotes("Integration test session " + notesSuffix);
        return pomodoroSessionRepository.save(session);
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        pomodoroSessionRepository.deleteAll(); // Ensure a clean state before and after each test
    }

    @Test
    void shouldCreatePomodoroSession() {
        // Given - now create a DTO for the request
        PomodoroSessionCreationDTO newSessionDTO = new PomodoroSessionCreationDTO();
        newSessionDTO.setStartTime(Instant.now());
        newSessionDTO.setEndTime(Instant.now().plusSeconds(30 * 60));
        newSessionDTO.setDuration(30);
        newSessionDTO.setScore((short) 5);
        newSessionDTO.setNotes("Deep work session.");

        // When - post the DTO and expect a ResponseDTO
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.postForEntity(
                "/api/pomodoro", // Ensure this matches your controller's @RequestMapping
                newSessionDTO, // Sending CreationDTO
                PomodoroSessionResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getDuration()).isEqualTo(30);
        assertThat(response.getBody().getScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getNotes()).isEqualTo("Deep work session.");

        // Verify it's persisted in the database as an actual Entity
        Optional<PomodoroSession> persistedEntity = pomodoroSessionRepository.findById(response.getBody().getId());
        assertThat(persistedEntity).isPresent();
        assertThat(persistedEntity.get().getDuration()).isEqualTo(30);
        assertThat(persistedEntity.get().getNotes()).isEqualTo("Deep work session.");
    }

    @Test
    void shouldGetAllPomodoroSessions() {
        // Given
        createTestPomodoroSessionInDb("1");
        createTestPomodoroSessionInDb("2");

        // When - now expect a List of ResponseDTOs
        ResponseEntity<List<PomodoroSessionResponseDTO>> response = restTemplate.exchange(
                "/api/pomodoro",
                HttpMethod.GET,
                null, // No request body for GET
                new ParameterizedTypeReference<List<PomodoroSessionResponseDTO>>() {} // Use ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        // Assert that the items in the list are of the correct DTO type and have expected data
        assertThat(response.getBody().get(0).getId()).isNotNull();
        assertThat(response.getBody().get(0).getDuration()).isEqualTo(25);
        assertThat(response.getBody().get(0).getNotes()).contains("Integration test session");
    }

    @Test
    void shouldGetPomodoroSessionById() {
        // Given
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("for lookup");

        // When - now expect a ResponseDTO
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.getForEntity(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                PomodoroSessionResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingSessionEntity.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(existingSessionEntity.getDuration());
        assertThat(response.getBody().getNotes()).isEqualTo(existingSessionEntity.getNotes());
    }

    @Test
    void shouldReturnNotFoundForNonExistentPomodoroSession() {
        // When
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.getForEntity(
                "/api/pomodoro/999", // Non-existent ID
                PomodoroSessionResponseDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdatePomodoroSession() {
        // Given
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("to be updated");

        PomodoroSessionCreationDTO updateDTO = new PomodoroSessionCreationDTO(); // Create DTO for update request
        updateDTO.setStartTime(Instant.now().minusSeconds(120));
        updateDTO.setEndTime(Instant.now().plusSeconds(60 * 60)); // 60 minutes
        updateDTO.setDuration(60);
        updateDTO.setScore((short) 5);
        updateDTO.setNotes("Updated a super focused session!");

        // When
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.exchange(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO), // Send CreationDTO/UpdateDTO as the request body
                PomodoroSessionResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingSessionEntity.getId());
        assertThat(response.getBody().getDuration()).isEqualTo(60);
        assertThat(response.getBody().getScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getNotes()).isEqualTo("Updated a super focused session!");

        // Verify changes in the database as an Entity
        PomodoroSession fetchedFromDb = pomodoroSessionRepository.findById(existingSessionEntity.getId()).orElse(null);
        assertThat(fetchedFromDb).isNotNull();
        assertThat(fetchedFromDb.getDuration()).isEqualTo(60);
        assertThat(fetchedFromDb.getNotes()).isEqualTo("Updated a super focused session!");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPomodoroSession() {
        // Given
        PomodoroSessionCreationDTO updateDTO = new PomodoroSessionCreationDTO();
        updateDTO.setStartTime(Instant.now());
        updateDTO.setEndTime(Instant.now().plusSeconds(25 * 60));
        updateDTO.setDuration(25);
        updateDTO.setScore((short) 4);
        updateDTO.setNotes("Non-existent update");

        // When
        ResponseEntity<PomodoroSessionResponseDTO> response = restTemplate.exchange(
                "/api/pomodoro/999", // Non-existent ID
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO),
                PomodoroSessionResponseDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeletePomodoroSession() {
        // Given
        PomodoroSession existingSessionEntity = createTestPomodoroSessionInDb("to be deleted");

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/pomodoro/" + existingSessionEntity.getId(),
                HttpMethod.DELETE,
                null, // No request body for DELETE
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        // Verify deletion in the database
        assertThat(pomodoroSessionRepository.findById(existingSessionEntity.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPomodoroSession() {
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/pomodoro/999", // Non-existent ID
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}