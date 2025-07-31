package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryCreationDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class MoodEntryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    // Helper method to create a MoodEntry in the DB directly for test setup
    private MoodEntry createTestMoodEntryInDb(String notesSuffix) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore((short) 3);
        moodEntry.setDateTime(Instant.now());
        moodEntry.setFactors(Arrays.asList("Integration", "Setup"));
        moodEntry.setNotes("Integration test entry " + notesSuffix);
        return moodEntryRepository.save(moodEntry);
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        moodEntryRepository.deleteAll();
    }

    @Test
    void shouldCreateMoodEntry() {
        // Given - now create a DTO for the request
        MoodEntryCreationDTO newMoodEntryDTO = new MoodEntryCreationDTO();
        newMoodEntryDTO.setMoodScore((short) 4);
        newMoodEntryDTO.setDateTime(Instant.now());
        newMoodEntryDTO.setFactors(Arrays.asList("Sunshine", "Good Sleep"));
        newMoodEntryDTO.setNotes("Feeling good!");

        // When - post the DTO and expect a ResponseDTO
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/mood",
                newMoodEntryDTO, // Sending CreationDTO
                MoodEntryResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getMoodScore()).isEqualTo((short) 4);
        assertThat(response.getBody().getDateTime()).isNotNull();
        assertThat(response.getBody().getFactors()).containsExactly("Sunshine", "Good Sleep");
        assertThat(response.getBody().getNotes()).isEqualTo("Feeling good!");

        // Verify it's persisted in the database as an actual Entity
        Optional<MoodEntry> persistedEntity = moodEntryRepository.findById(response.getBody().getId());
        assertThat(persistedEntity).isPresent();
        assertThat(persistedEntity.get().getMoodScore()).isEqualTo((short) 4);
        assertThat(persistedEntity.get().getNotes()).isEqualTo("Feeling good!");
    }

    @Test
    void shouldGetAllMoodEntries() {
        // Given
        createTestMoodEntryInDb("1");
        createTestMoodEntryInDb("2");

        // When - now expect a List of ResponseDTOs
        ResponseEntity<List<MoodEntryResponseDTO>> response = restTemplate.exchange(
                "/api/mood",
                HttpMethod.GET,
                null, // No request body for GET
                new ParameterizedTypeReference<List<MoodEntryResponseDTO>>() {} // Use ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        // Assert that the items in the list are of the correct DTO type and have expected data
        assertThat(response.getBody().get(0).getId()).isNotNull();
        assertThat(response.getBody().get(0).getMoodScore()).isEqualTo((short) 3);
        assertThat(response.getBody().get(0).getNotes()).contains("Integration test entry");
    }

    @Test
    void shouldGetMoodEntryById() {
        // Given
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("for lookup");

        // When - now expect a ResponseDTO
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.getForEntity(
                "/api/mood/" + existingMoodEntity.getId(),
                MoodEntryResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingMoodEntity.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(existingMoodEntity.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(existingMoodEntity.getNotes());
    }

    @Test
    void shouldReturnNotFoundForNonExistentMoodEntry() {
        // When
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.getForEntity(
                "/api/mood/999", // Non-existent ID
                MoodEntryResponseDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateMoodEntry() {
        // Given
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be updated"); // Still create entity in DB

        MoodEntryCreationDTO updateDTO = new MoodEntryCreationDTO(); // Create DTO for update request
        updateDTO.setMoodScore((short) 5);
        updateDTO.setDateTime(Instant.now().plusSeconds(60));
        updateDTO.setFactors(Arrays.asList("Success", "Good Food"));
        updateDTO.setNotes("Feeling amazing after update!");

        // When
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO), // Send CreationDTO/UpdateDTO as the request body
                MoodEntryResponseDTO.class // Expecting ResponseDTO
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingMoodEntity.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getFactors()).containsExactly("Success", "Good Food");
        assertThat(response.getBody().getNotes()).isEqualTo("Feeling amazing after update!");

        // Verify changes in the database as an Entity
        MoodEntry fetchedFromDb = moodEntryRepository.findById(existingMoodEntity.getId()).orElse(null);
        assertThat(fetchedFromDb).isNotNull();
        assertThat(fetchedFromDb.getMoodScore()).isEqualTo((short) 5);
        assertThat(fetchedFromDb.getNotes()).isEqualTo("Feeling amazing after update!");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMoodEntry() {
        // Given
        MoodEntryCreationDTO updateDTO = new MoodEntryCreationDTO();
        updateDTO.setMoodScore((short) 5);
        updateDTO.setDateTime(Instant.now());
        updateDTO.setNotes("Non-existent update");

        // When
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.exchange(
                "/api/mood/999", // Non-existent ID
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO),
                MoodEntryResponseDTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteMoodEntry() {
        // Given
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be deleted");

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.DELETE,
                null, // No request body for DELETE
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        // Verify deletion in the database
        assertThat(moodEntryRepository.findById(existingMoodEntity.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMoodEntry() {
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/999", // Non-existent ID
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
