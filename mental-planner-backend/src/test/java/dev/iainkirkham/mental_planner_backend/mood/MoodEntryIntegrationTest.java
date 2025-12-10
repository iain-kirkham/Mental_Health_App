package dev.iainkirkham.mental_planner_backend.mood;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MoodEntry API endpoints.
 * Uses Testcontainers for PostgreSQL, disables security for testing,
 * and mocks AuthenticationContext to provide a consistent test user.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class, TestSecurityConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class MoodEntryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    // Use a fixed instant for deterministic tests
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    /**
     * Creates a mood entry directly in the database for test setup purposes.
     * Sets the userId to match the test authentication context.
     *
     * @param notesSuffix A unique suffix to append to the notes field for test identification
     * @return The persisted MoodEntry entity with generated ID and userId
     */
    private MoodEntry createTestMoodEntryInDb(String notesSuffix) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore((short) 3);
        moodEntry.setDateTime(FIXED_NOW);
        moodEntry.setFactors(Arrays.asList("Integration", "Setup"));
        moodEntry.setNotes("Integration test entry " + notesSuffix);
        moodEntry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        return moodEntryRepository.save(moodEntry);
    }

    // Helper to create a MoodEntry at a specific Instant (reduces duplication and flakiness)
    private void createMoodEntryAt(Instant dateTime, short score, String notes) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore(score);
        moodEntry.setDateTime(dateTime);
        moodEntry.setFactors(List.of("Integration"));
        moodEntry.setNotes(notes);
        moodEntry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        moodEntryRepository.save(moodEntry);
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
        // Arrange: Prepare a new mood entry creation request using request DTO
        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO newMoodEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        newMoodEntry.setMoodScore((short) 4);
        newMoodEntry.setDateTime(FIXED_NOW);
        newMoodEntry.setFactors(Arrays.asList("Sunshine", "Good Sleep"));
        newMoodEntry.setNotes("Feeling good!");

        // Act: Send POST request to create mood entry and expect response DTO
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.postForEntity(
                "/api/mood",
                newMoodEntry,
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        // Assert: Verify successful creation with HTTP 201 and correct response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(created -> {
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getMoodScore()).isEqualTo((short) 4);
            assertThat(created.getDateTime()).isNotNull();
            assertThat(created.getFactors()).containsExactly("Sunshine", "Good Sleep");
            assertThat(created.getNotes()).isEqualTo("Feeling good!");

            // Assert: Verify the mood entry was actually persisted to the database with correct userId
            Optional<MoodEntry> persistedEntity = moodEntryRepository.findById(created.getId());
            assertThat(persistedEntity).isPresent();
            assertThat(persistedEntity.get().getMoodScore()).isEqualTo((short) 4);
            assertThat(persistedEntity.get().getNotes()).isEqualTo("Feeling good!");
            assertThat(persistedEntity.get().getUserId()).isEqualTo(TestAuthenticationConfig.TEST_USER_ID);
        });
    }

    @Test
    void shouldGetAllMoodEntries() {
        // Arrange: Create test data - two mood entries with different identifiers
        createTestMoodEntryInDb("1");
        createTestMoodEntryInDb("2");

        // Act: Send GET request to retrieve all mood entries and expect response DTOs
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood",
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
                .satisfies(entry -> {
                    assertThat(entry.getId()).isNotNull();
                    assertThat(entry.getMoodScore()).isEqualTo((short) 3);
                    assertThat(entry.getNotes()).contains("Integration test entry");
                });
    }

    @Test
    void shouldGetMoodEntryById() {
        // Arrange: Create a specific mood entry to retrieve by ID
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("for lookup");

        // Act: Send GET request for specific mood entry by ID and expect response DTO
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.getForEntity(
                "/api/mood/" + existingMoodEntity.getId(),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        // Assert: Verify successful retrieval with matching data from database entity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(existingMoodEntity.getId());
            assertThat(body.getMoodScore()).isEqualTo(existingMoodEntity.getMoodScore());
            assertThat(body.getNotes()).isEqualTo(existingMoodEntity.getNotes());
        });
    }

    @Test
    void shouldReturnNotFoundForNonExistentMoodEntry() {
        // Act: Attempt to retrieve mood entry using non-existent ID
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.getForEntity(
                "/api/mood/999",
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        // Assert: Verify proper HTTP 404 Not Found response for missing resource
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateMoodEntry() {
        // Arrange: Create existing mood entry in database to update
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be updated");

        // Arrange: Prepare update data with modified values using request DTO
        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO updateEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        updateEntry.setMoodScore((short) 5);
        updateEntry.setDateTime(FIXED_NOW.plusSeconds(60));
        updateEntry.setFactors(Arrays.asList("Success", "Good Food"));
        updateEntry.setNotes("Feeling amazing after update!");

        // Act: Send PUT request to update existing mood entry and expect response DTO
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateEntry),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        // Assert: Verify successful update with HTTP 200 and updated response data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(existingMoodEntity.getId());
            assertThat(body.getMoodScore()).isEqualTo((short) 5);
            assertThat(body.getFactors()).containsExactly("Success", "Good Food");
            assertThat(body.getNotes()).isEqualTo("Feeling amazing after update!");

            // Assert: Verify the changes were persisted to the database
            MoodEntry fetchedFromDb = moodEntryRepository.findById(existingMoodEntity.getId()).orElse(null);
            assertThat(fetchedFromDb).isNotNull();
            assertThat(fetchedFromDb.getMoodScore()).isEqualTo((short) 5);
            assertThat(fetchedFromDb.getNotes()).isEqualTo("Feeling amazing after update!");
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMoodEntry() {
        // Arrange: Prepare update data for non-existent mood entry using request DTO
        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO updateEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        updateEntry.setMoodScore((short) 5);
        updateEntry.setDateTime(FIXED_NOW);
        updateEntry.setFactors(Arrays.asList("Nothing"));
        updateEntry.setNotes("Non-existent update");

        // Act: Attempt to update mood entry that doesn't exist
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.exchange(
                "/api/mood/999",
                HttpMethod.PUT,
                new HttpEntity<>(updateEntry),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
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

    // --- New date-range integration tests ---

    @Test
    void getMoodEntriesByDateRange_ShouldReturnOnlyEntriesInRange() {
        // Arrange: create three entries at different times
        Instant now = FIXED_NOW;
        createMoodEntryAt(now.minus(10, ChronoUnit.DAYS), (short)2, "older");
        createMoodEntryAt(now.minus(5, ChronoUnit.DAYS), (short)3, "middle");
        createMoodEntryAt(now, (short)4, "recent");

        // Act: query for entries from 7 days ago to now -> should return middle and recent
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        String end = now.plus(1, ChronoUnit.DAYS).toString();

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .extracting(dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO::getNotes)
                .containsExactly("recent", "middle");
    }

    @Test
    void getMoodEntriesByDateRange_ShouldReturnNoContentWhenNoMatches() {
        // Arrange: create an entry outside the queried window
        Instant now = FIXED_NOW;
        createMoodEntryAt(now.minus(30, ChronoUnit.DAYS), (short)3, "out-of-range");

        // Act: query recent dates where there are no entries
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        String end = now.toString();

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: controller returns 204 No Content when list empty
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getMoodEntriesByDateRange_InvalidDateFormat_ReturnsBadRequest() {
        // Act: call endpoint with invalid date format
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/mood?startDate=not-a-date&endDate=also-not-a-date",
                HttpMethod.GET,
                null,
                String.class
        );

        // Assert: Spring should return 400 Bad Request for unparsable Instants
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getMoodEntriesByDateRange_PartialStartDate_ReturnsAllEntries() {
        // Arrange: create three entries at different times
        Instant now = FIXED_NOW;
        createMoodEntryAt(now.minus(10, ChronoUnit.DAYS), (short)2, "older");
        createMoodEntryAt(now.minus(5, ChronoUnit.DAYS), (short)3, "middle");
        createMoodEntryAt(now, (short)4, "recent");

        // Act: call with only startDate
        String start = now.minus(7, ChronoUnit.DAYS).toString();
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + start,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: current controller behavior is to ignore partial ranges (returns all) - assert this
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(3);
    }

    @Test
    void getMoodEntriesByDateRange_PartialEndDate_ReturnsAllEntries() {
        // Arrange: create entries
        Instant now = FIXED_NOW;
        // use the time-aware helper for consistent timestamps across tests
        createMoodEntryAt(now.minus(2, ChronoUnit.DAYS), (short)3, "A");
        createMoodEntryAt(now.minus(1, ChronoUnit.DAYS), (short)3, "B");
        createMoodEntryAt(now, (short)3, "C");

        // Act: call with only endDate
        String end = now.toString();
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: controller currently treats partial params as no-op -> returns all
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(3);
    }

    @Test
    void getMoodEntriesByDateRange_BoundaryInclusivity_StartAndEndInclusive() {
        // Arrange: set explicit entries at the start and end boundaries
        Instant now = FIXED_NOW;
        Instant startInstant = now.minus(7, ChronoUnit.DAYS);
        createMoodEntryAt(startInstant, (short)1, "start");
        createMoodEntryAt(now, (short)5, "end");

        // Act: query inclusive window
        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + startInstant + "&endDate=" + now,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert: results should include both boundary entries; ordering is newest-first
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO::getNotes)
                .containsExactly("end", "start");
    }

}
