package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.config.TestAuthenticationConfig;
import dev.iainkirkham.mental_planner_backend.config.TestSecurityConfiguration;
import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
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
@AutoConfigureTestRestTemplate
@Import({TestcontainersConfiguration.class, TestAuthenticationConfig.class, TestSecurityConfiguration.class})
@org.springframework.test.context.ActiveProfiles("test")
class MoodEntryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    // Use a fixed instant for deterministic tests
    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    private MoodEntry createTestMoodEntryInDb(String notesSuffix) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore((short) 3);
        moodEntry.setDateTime(FIXED_NOW);
        moodEntry.setFactors(Arrays.asList("Integration", "Setup"));
        moodEntry.setNotes("Integration test entry " + notesSuffix);
        moodEntry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        return moodEntryRepository.save(moodEntry);
    }

    private void createMoodEntryAt(Instant dateTime, short score, String notes) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setMoodScore(score);
        moodEntry.setDateTime(dateTime);
        moodEntry.setFactors(List.of("Integration"));
        moodEntry.setNotes(notes);
        moodEntry.setUserId(TestAuthenticationConfig.TEST_USER_ID);
        moodEntryRepository.save(moodEntry);
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        moodEntryRepository.deleteAll();
    }

    @Test
    void shouldCreateMoodEntry() {
        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO newMoodEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        newMoodEntry.setMoodScore((short) 4);
        newMoodEntry.setDateTime(FIXED_NOW);
        newMoodEntry.setFactors(Arrays.asList("Sunshine", "Good Sleep"));
        newMoodEntry.setNotes("Feeling good!");

        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.postForEntity(
                "/api/mood",
                newMoodEntry,
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(created -> {
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getMoodScore()).isEqualTo((short) 4);
            assertThat(created.getDateTime()).isNotNull();
            assertThat(created.getFactors()).containsExactly("Sunshine", "Good Sleep");
            assertThat(created.getNotes()).isEqualTo("Feeling good!");

            Optional<MoodEntry> persistedEntity = moodEntryRepository.findById(created.getId());
            assertThat(persistedEntity).isPresent();
            assertThat(persistedEntity.get().getMoodScore()).isEqualTo((short) 4);
            assertThat(persistedEntity.get().getNotes()).isEqualTo("Feeling good!");
            assertThat(persistedEntity.get().getUserId()).isEqualTo(TestAuthenticationConfig.TEST_USER_ID);
        });
    }

    @Test
    void shouldGetAllMoodEntries() {
        createTestMoodEntryInDb("1");
        createTestMoodEntryInDb("2");

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

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
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("for lookup");

        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.getForEntity(
                "/api/mood/" + existingMoodEntity.getId(),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

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
        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.getForEntity(
                "/api/mood/999",
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateMoodEntry() {
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be updated");

        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO updateEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        updateEntry.setMoodScore((short) 5);
        updateEntry.setDateTime(FIXED_NOW.plusSeconds(60));
        updateEntry.setFactors(Arrays.asList("Success", "Good Food"));
        updateEntry.setNotes("Feeling amazing after update!");

        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateEntry),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(existingMoodEntity.getId());
            assertThat(body.getMoodScore()).isEqualTo((short) 5);
            assertThat(body.getFactors()).containsExactly("Success", "Good Food");
            assertThat(body.getNotes()).isEqualTo("Feeling amazing after update!");

            MoodEntry fetchedFromDb = moodEntryRepository.findById(existingMoodEntity.getId()).orElse(null);
            assertThat(fetchedFromDb).isNotNull();
            assertThat(fetchedFromDb.getMoodScore()).isEqualTo((short) 5);
            assertThat(fetchedFromDb.getNotes()).isEqualTo("Feeling amazing after update!");
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMoodEntry() {
        dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO updateEntry =
            new dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO();
        updateEntry.setMoodScore((short) 5);
        updateEntry.setDateTime(FIXED_NOW);
        updateEntry.setFactors(Arrays.asList("Nothing"));
        updateEntry.setNotes("Non-existent update");

        ResponseEntity<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO> response =
            restTemplate.exchange(
                "/api/mood/999",
                HttpMethod.PUT,
                new HttpEntity<>(updateEntry),
                dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteMoodEntry() {
        MoodEntry existingMoodEntity = createTestMoodEntryInDb("to be deleted");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/" + existingMoodEntity.getId(),
                HttpMethod.DELETE,
                null, // No request body required for DELETE operation
                Void.class // No response body expected for successful deletion
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(moodEntryRepository.findById(existingMoodEntity.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMoodEntry() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/mood/999", // ID that doesn't exist in database
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- New date-range integration tests ---

    @Test
    void getMoodEntriesByDateRange_ShouldReturnOnlyEntriesInRange() {
        Instant now = FIXED_NOW;
        createMoodEntryAt(now.minus(10, ChronoUnit.DAYS), (short)2, "older");
        createMoodEntryAt(now.minus(5, ChronoUnit.DAYS), (short)3, "middle");
        createMoodEntryAt(now, (short)4, "recent");

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
        Instant now = FIXED_NOW;
        createMoodEntryAt(now.minus(30, ChronoUnit.DAYS), (short)3, "out-of-range");

        String start = now.minus(7, ChronoUnit.DAYS).toString();
        String end = now.toString();

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getMoodEntriesByDateRange_InvalidDateFormat_ReturnsBadRequest() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/mood?startDate=not-a-date&endDate=also-not-a-date",
                HttpMethod.GET,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getMoodEntriesByDateRange_PartialDateParam_ReturnsAllEntries() {
        createTestMoodEntryInDb("A");
        createTestMoodEntryInDb("B");
        createTestMoodEntryInDb("C");

        // controller treats a lone startDate or endDate as a no-op -> returns all entries
        String start = FIXED_NOW.minus(7, ChronoUnit.DAYS).toString();
        assertThat(restTemplate.exchange(
                "/api/mood?startDate=" + start,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>>() {})
                .getBody()).isNotNull().hasSize(3);

        String end = FIXED_NOW.toString();
        assertThat(restTemplate.exchange(
                "/api/mood?endDate=" + end,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>>() {})
                .getBody()).isNotNull().hasSize(3);
    }

    @Test
    void getMoodEntriesByDateRange_BoundaryInclusivity_StartAndEndInclusive() {
        Instant now = FIXED_NOW;
        Instant startInstant = now.minus(7, ChronoUnit.DAYS);
        createMoodEntryAt(startInstant, (short)1, "start");
        createMoodEntryAt(now, (short)5, "end");

        ResponseEntity<List<dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO>> response =
            restTemplate.exchange(
                "/api/mood?startDate=" + startInstant + "&endDate=" + now,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO::getNotes)
                .containsExactly("end", "start");
    }

}
