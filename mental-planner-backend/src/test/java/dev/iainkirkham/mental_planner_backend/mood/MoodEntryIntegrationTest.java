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

    /**
     * Creates a mood entry directly in the database for test setup purposes.
     *
     * @param notesSuffix A unique suffix to append to the notes field for test identification
     * @return The persisted MoodEntry entity with generated ID
     */
    private MoodEntry createTestMoodEntryInDb(String notesSuffix) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore((short) 3);
        moodEntry.setDateTime(Instant.now());
        moodEntry.setFactors(Arrays.asList("Integration", "Setup"));
        moodEntry.setNotes("Integration test entry " + notesSuffix);
        return moodEntryRepository.save(moodEntry);
    }

    /**
     * Ensures a clean database state before and after each test.
     * This prevents test interference and maintains isolation.
     */
    @BeforeEach
    @AfterEach
    void cleanUp() {
        moodEntryRepository.deleteAll();
    }

    @Test
    void shouldCreateMoodEntry() {
        // Arrange: Prepare a new mood entry creation request with sample data
        MoodEntryCreationDTO newMoodEntryDTO = new MoodEntryCreationDTO();
        newMoodEntryDTO.setMoodScore((short) 4);
        newMoodEntryDTO.setDateTime(Instant.now());
        newMoodEntryDTO.setFactors(Arrays.asList("Sunshine", "Good Sleep"));
        newMoodEntryDTO.setNotes("Feeling good!");

        // Act: Send POST request to create mood entry and expect response DTO
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.postForEntity(
                "/api/mood",
                newMoodEntryDTO, // Request body: CreationDTO
                MoodEntryResponseDTO.class // Expected response type: ResponseDTO
        );

        // Assert: Verify successful creation with HTTP 201 and correct response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getMoodScore()).isEqualTo((short) 4);
        assertThat(response.getBody().getDateTime()).isNotNull();
        assertThat(response.getBody().getFactors()).containsExactly("Sunshine", "Good Sleep");
        assertThat(response.getBody().getNotes()).isEqualTo("Feeling good!");

        // Assert: Verify the mood entry was actually persisted to the database
        Optional<MoodEntry> persistedEntity = moodEntryRepository.findById(response.getBody().getId());
        assertThat(persistedEntity).isPresent();
        assertThat(persistedEntity.get().getMoodScore()).isEqualTo((short) 4);
        assertThat(persistedEntity.get().getNotes()).isEqualTo("Feeling good!");
    }

    @Test
    void shouldGetAllMoodEntries() {
        // Arrange: Create test data - two mood entries with different identifiers
        createTestMoodEntryInDb("1");
        createTestMoodEntryInDb("2");

        // Act: Send GET request to retrieve all mood entries as response DTOs
        ResponseEntity<List<MoodEntryResponseDTO>> response = restTemplate.exchange(
                "/api/mood",
                HttpMethod.GET,
                null, // No request body required for GET operation
                new ParameterizedTypeReference<List<MoodEntryResponseDTO>>() {} // Use ResponseDTO
        );

        // Assert: Verify successful retrieval with correct count and data structure
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        // Assert: Verify the returned items are properly formatted ResponseDTOs with expected content
        assertThat(response.getBody().get(0).getId()).isNotNull();
        assertThat(response.getBody().get(0).getMoodScore()).isEqualTo((short) 3);
        assertThat(response.getBody().get(0).getNotes()).contains("Integration test entry");
    }

    @Test
    void shouldGetMoodEntryById() {
        // Arrange: Create a specific mood entry to retrieve by ID
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("for lookup");

        // Act: Send GET request for specific mood entry by ID, expecting response DTO
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.getForEntity(
                "/api/mood/" + existingMoodEntity.getId(),
                MoodEntryResponseDTO.class // Expected response type: ResponseDTO
        );

        // Assert: Verify successful retrieval with matching data from database entity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingMoodEntity.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(existingMoodEntity.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(existingMoodEntity.getNotes());
    }

    @Test
    void shouldReturnNotFoundForNonExistentMoodEntry() {
        // Act: Attempt to retrieve mood entry using non-existent ID
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.getForEntity(
                "/api/mood/999", // Non-existent ID
                MoodEntryResponseDTO.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateMoodEntry() {
        // Arrange: Create existing mood entry in database to update
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be updated"); // Still create entity in DB

        // Arrange: Prepare update data with modified values
        MoodEntryCreationDTO updateDTO = new MoodEntryCreationDTO(); // Create DTO for update request
        updateDTO.setMoodScore((short) 5);
        updateDTO.setDateTime(Instant.now().plusSeconds(60));
        updateDTO.setFactors(Arrays.asList("Success", "Good Food"));
        updateDTO.setNotes("Feeling amazing after update!");

        // Act: Send PUT request to update existing mood entry
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO), // Request body: CreationDTO with updated values
                MoodEntryResponseDTO.class // Expected response type: ResponseDTO
        );

        // Assert: Verify successful update with HTTP 200 and updated response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(existingMoodEntity.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo((short) 5);
        assertThat(response.getBody().getFactors()).containsExactly("Success", "Good Food");
        assertThat(response.getBody().getNotes()).isEqualTo("Feeling amazing after update!");

        // Assert: Verify the changes were persisted to the database
        MoodEntry fetchedFromDb = moodEntryRepository.findById(existingMoodEntity.getId()).orElse(null);
        assertThat(fetchedFromDb).isNotNull();
        assertThat(fetchedFromDb.getMoodScore()).isEqualTo((short) 5);
        assertThat(fetchedFromDb.getNotes()).isEqualTo("Feeling amazing after update!");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMoodEntry() {
        // Arrange: Prepare update data for non-existent mood entry
        MoodEntryCreationDTO updateDTO = new MoodEntryCreationDTO();
        updateDTO.setMoodScore((short) 5);
        updateDTO.setDateTime(Instant.now());
        updateDTO.setNotes("Non-existent update");

        // Act: Attempt to update mood entry that doesn't exist
        ResponseEntity<MoodEntryResponseDTO> response = restTemplate.exchange(
                "/api/mood/999", // ID that doesn't exist in database
                HttpMethod.PUT,
                new HttpEntity<>(updateDTO),
                MoodEntryResponseDTO.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteMoodEntry() {
        // Arrange: Create mood entry in database to be deleted
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be deleted");

        // Act: Send DELETE request to remove the mood entry
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.DELETE,
                null, // No request body required for DELETE operation
                Void.class // No response body expected for successful deletion
        );

        // Assert: Verify successful deletion with HTTP 204 No Content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        // Assert: Verify the mood entry was actually removed from the database
        assertThat(moodEntryRepository.findById(existingMoodEntity.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMoodEntry() {
        // Act: Attempt to delete mood entry that doesn't exist
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/999", // ID that doesn't exist in database
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
