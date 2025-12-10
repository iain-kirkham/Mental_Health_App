package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryRequestDTO;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodEntryControllerTest {

    @Mock
    private MoodEntryService moodEntryService;

    @InjectMocks
    private MoodEntryController moodEntryController;

    private static final Instant FIXED_NOW = Instant.parse("2025-12-01T00:00:00Z");

    // Test data representing different mood entry scenarios
    private MoodEntryRequestDTO testRequestDTO;
    private MoodEntryResponseDTO savedResponseDTO;
    private MoodEntryResponseDTO updatedResponseDTO;

    // Test userId for consistent authentication context (not exposed in DTOs)
    private static final String TEST_USER_ID = "user_test123";

    /**
     * Initializes test data before each test execution.
     * Creates realistic test objects representing:
     * - Client request DTO with factors and notes (no userId/ID)
     * - Service layer response DTOs with generated IDs
     * - Update scenarios with improved mood states
     * Fresh objects prevent test interference and ensure isolation.
     *
     * Note: userId is automatically set by the service layer via AuthenticationContext,
     * and is NOT exposed in DTOs for security.
     */
    @BeforeEach
    void setUp() {
        // Simulates challenging day mood entry from client
        testRequestDTO = new MoodEntryRequestDTO();
        testRequestDTO.setMoodScore((short) 4);
        testRequestDTO.setDateTime(FIXED_NOW);
        testRequestDTO.setFactors(List.of("Work", "Stress"));
        testRequestDTO.setNotes("Had a tough day.");

        // Expected service response after successful creation
        savedResponseDTO = new MoodEntryResponseDTO();
        savedResponseDTO.setId(1L);
        savedResponseDTO.setMoodScore((short) 4);
        savedResponseDTO.setDateTime(FIXED_NOW);
        savedResponseDTO.setFactors(List.of("Work", "Stress"));
        savedResponseDTO.setNotes("Had a tough day.");

        // Improved mood state for update testing
        updatedResponseDTO = new MoodEntryResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setMoodScore((short) 5);
        updatedResponseDTO.setDateTime(FIXED_NOW.plusSeconds(10));
        updatedResponseDTO.setFactors(List.of("Achievement", "Relaxation"));
        updatedResponseDTO.setNotes("Feeling fantastic after finishing the task!");
    }

    @Test
    void createMoodEntry_ShouldReturnCreatedMoodEntry() {
        // Arrange: Mock service to simulate successful creation
        when(moodEntryService.createMoodEntry(any(MoodEntryRequestDTO.class))).thenReturn(savedResponseDTO);

        // Act: Call controller creation endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.createMoodEntry(testRequestDTO);

        // Assert: Verify creation success and data integrity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).createMoodEntry(testRequestDTO);
    }

    @Test
    void getAllMoodEntries_ShouldReturnAllMoodEntries() {
        // Arrange: Mock service to return multiple mood entries
        MoodEntryResponseDTO secondEntry = new MoodEntryResponseDTO();
        secondEntry.setId(2L);
        secondEntry.setMoodScore((short) 3);
        secondEntry.setDateTime(FIXED_NOW.plusSeconds(7200));
        List<MoodEntryResponseDTO> expectedEntries = List.of(savedResponseDTO, secondEntry);
        when(moodEntryService.getAllMoodEntries()).thenReturn(expectedEntries);

        // Act: Call controller list endpoint (with null dates = get all)
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries(null, null);

        // Assert: Verify response structure and content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .first()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).getAllMoodEntries();
    }

    @Test
    void getAllMoodEntries_ShouldReturnNoContentWhenEmpty() {
        // Arrange: Mock service to return empty collection
        when(moodEntryService.getAllMoodEntries()).thenReturn(List.of());

        // Act: Call controller list endpoint
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries(null, null);

        // Assert: Verify empty response handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(moodEntryService).getAllMoodEntries();
    }

    @Test
    void getMoodEntryById_ShouldReturnMoodEntryWhenFound() {
        // Arrange: Mock service to return specific mood entry
        when(moodEntryService.getMoodEntryById(anyLong())).thenReturn(savedResponseDTO);

        // Act: Call controller get-by-id endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.getMoodEntryById(1L);

        // Assert: Verify successful retrieval
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedResponseDTO);
        verify(moodEntryService).getMoodEntryById(1L);
    }

    @Test
    void getMoodEntryById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw not found exception
        when(moodEntryService.getMoodEntryById(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert: Verify exception propagates to Spring's exception handler
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.getMoodEntryById(99L));
        verify(moodEntryService).getMoodEntryById(99L);
    }

    @Test
    void updateMoodEntry_ShouldReturnUpdatedMoodEntryWhenFound() {
        // Arrange: Mock service to return updated mood entry
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryRequestDTO.class))).thenReturn(updatedResponseDTO);

        // Act: Call controller update endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.updateMoodEntry(1L, testRequestDTO);

        // Assert: Verify update success and data changes
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedResponseDTO);
        verify(moodEntryService).updateMoodEntry(1L, testRequestDTO);
    }

    @Test
    void updateMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found for update"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.updateMoodEntry(99L, testRequestDTO));
        verify(moodEntryService).updateMoodEntry(99L, testRequestDTO);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNoContentWhenFound() {
        // Arrange: Mock service to complete deletion without error
        doNothing().when(moodEntryService).deleteMoodEntry(anyLong());

        // Act: Call controller delete endpoint
        ResponseEntity<Void> response = moodEntryController.deleteMoodEntry(1L);

        // Assert: Verify successful deletion response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(moodEntryService).deleteMoodEntry(1L);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        doThrow(new ResourceNotFoundException("Not found for delete")).when(moodEntryService).deleteMoodEntry(anyLong());

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.deleteMoodEntry(99L));
        verify(moodEntryService).deleteMoodEntry(99L);
    }
}