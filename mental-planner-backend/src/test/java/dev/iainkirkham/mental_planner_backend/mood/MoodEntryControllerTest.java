package dev.iainkirkham.mental_planner_backend.mood;

import dev.iainkirkham.mental_planner_backend.exception.ResourceNotFoundException;
import dev.iainkirkham.mental_planner_backend.mood.dto.MoodEntryCreationDTO;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoodEntryControllerTest {

    @Mock
    private MoodEntryService moodEntryService;

    @InjectMocks
    private MoodEntryController moodEntryController;

    private MoodEntry testMoodEntry;

    // Test data representing different mood entry scenarios
    private MoodEntryCreationDTO testCreationDTO;
    private MoodEntryResponseDTO testResponseDTO;
    private MoodEntryResponseDTO updatedResponseDTO;

    /**
     * Initializes test data before each test execution.
     *
     * Creates realistic test objects representing:
     * - Client mood entry input with factors and notes
     * - Service layer responses with generated IDs
     * - Update scenarios with improved mood states
     *
     * Fresh objects prevent test interference and ensure isolation.
     */
    @BeforeEach
    void setUp() {
        // Simulates challenging day mood entry from client
        testCreationDTO = new MoodEntryCreationDTO();
        testCreationDTO.setMoodScore((short) 4);
        testCreationDTO.setDateTime(Instant.now());
        testCreationDTO.setFactors(Arrays.asList("Work", "Stress"));
        testCreationDTO.setNotes("Had a tough day.");

        // Expected service response after successful creation
        testResponseDTO = new MoodEntryResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setMoodScore((short) 4);
        testResponseDTO.setDateTime(Instant.now());
        testResponseDTO.setFactors(Arrays.asList("Work", "Stress"));
        testResponseDTO.setNotes("Had a tough day.");

        // Improved mood state for update testing
        updatedResponseDTO = new MoodEntryResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setMoodScore((short) 5);
        updatedResponseDTO.setDateTime(Instant.now().plusSeconds(10));
        updatedResponseDTO.setFactors(Arrays.asList("Achievement", "Relaxation"));
        updatedResponseDTO.setNotes("Feeling fantastic after finishing the task!");
    }

    @Test
    void createMoodEntry_ShouldReturnCreatedMoodEntry() {
        // Arrange: Mock service to simulate successful creation
        when(moodEntryService.createMoodEntry(any(MoodEntryCreationDTO.class))).thenReturn(testResponseDTO);

        // Act: Call controller creation endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.createMoodEntry(testCreationDTO);

        // Assert: Verify creation success and data integrity
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(testResponseDTO.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(testResponseDTO.getNotes());
        // Verify that the service method was called
        verify(moodEntryService, times(1)).createMoodEntry(testCreationDTO);
    }

    @Test
    void getAllMoodEntries_ShouldReturnAllMoodEntries() {
        // Arrange: Mock service to return multiple mood entries
        List<MoodEntryResponseDTO> expectedDtos = Arrays.asList(testResponseDTO, new MoodEntryResponseDTO()); // Add another dummy DTO
        when(moodEntryService.getAllMoodEntries()).thenReturn(expectedDtos);

        // Act: Call controller list endpoint
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries();

        // Assert: Verify response structure and content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getId()).isEqualTo(testResponseDTO.getId());
        verify(moodEntryService, times(1)).getAllMoodEntries();
    }

    @Test
    void getAllMoodEntries_ShouldReturnNoContentWhenEmpty() {
        // Arrange: Mock service to return empty collection
        when(moodEntryService.getAllMoodEntries()).thenReturn(Collections.emptyList());

        // Act: Call controller list endpoint
        ResponseEntity<List<MoodEntryResponseDTO>> response = moodEntryController.getAllMoodEntries();

        // Assert: Verify empty response handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(moodEntryService, times(1)).getAllMoodEntries();
    }

    @Test
    void getMoodEntryById_ShouldReturnMoodEntryWhenFound() {
        // Arrange: Mock service to return specific mood entry
        when(moodEntryService.getMoodEntryById(anyLong())).thenReturn(testResponseDTO);

        // Act: Call controller get-by-id endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.getMoodEntryById(1L);

        // Assert: Verify successful retrieval
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(testResponseDTO.getId());
        verify(moodEntryService, times(1)).getMoodEntryById(1L);
    }

    @Test
    void getMoodEntryById_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw not found exception
        when(moodEntryService.getMoodEntryById(anyLong())).thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert: Expect the controller to propagate the 404 status via @ResponseStatus
        // Spring's default exception handling will convert ResourceNotFoundException to 404
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.getMoodEntryById(99L));
        verify(moodEntryService, times(1)).getMoodEntryById(99L);
    }

    @Test
    void updateMoodEntry_ShouldReturnUpdatedMoodEntryWhenFound() {
        // Arrange: Mock service to return updated mood entry
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryCreationDTO.class))).thenReturn(updatedResponseDTO);

        // Act: Call controller update endpoint
        ResponseEntity<MoodEntryResponseDTO> response = moodEntryController.updateMoodEntry(1L, testCreationDTO); // Using testCreationDTO as update input

        // Assert: Verify update success and data changes
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(updatedResponseDTO.getId());
        assertThat(response.getBody().getMoodScore()).isEqualTo(updatedResponseDTO.getMoodScore());
        assertThat(response.getBody().getNotes()).isEqualTo(updatedResponseDTO.getNotes());
        verify(moodEntryService, times(1)).updateMoodEntry(1L, testCreationDTO);
    }

    @Test
    void updateMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        when(moodEntryService.updateMoodEntry(anyLong(), any(MoodEntryCreationDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not found for update"));

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.updateMoodEntry(99L, testCreationDTO));
        verify(moodEntryService, times(1)).updateMoodEntry(99L, testCreationDTO);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNoContentWhenFound() {
        // Arrange: Mock service to complete deletion without error
        doNothing().when(moodEntryService).deleteMoodEntry(anyLong());

        // Act: Call controller delete endpoint
        ResponseEntity<Void> response = moodEntryController.deleteMoodEntry(1L);

        // Assert: Verify successful deletion response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(moodEntryService, times(1)).deleteMoodEntry(1L);
    }

    @Test
    void deleteMoodEntry_ShouldReturnNotFoundWhenNotFound() {
        // Arrange: Mock service to throw exception for missing resource
        doThrow(new ResourceNotFoundException("Not found for delete")).when(moodEntryService).deleteMoodEntry(anyLong());

        // Act & Assert: Verify exception handling
        assertThrows(ResourceNotFoundException.class, () -> moodEntryController.deleteMoodEntry(99L));
        verify(moodEntryService, times(1)).deleteMoodEntry(99L);
    }
}